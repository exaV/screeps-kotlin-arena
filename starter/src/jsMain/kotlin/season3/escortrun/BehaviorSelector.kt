package season3.escortrun

import screeps.api.Creep
import screeps.api.HEAL_POWER

private fun calcHealDeficit(creeps: List<Creep>): Int =
    creeps.sumOf { (it.hitsMax - it.hits).coerceAtLeast(0) }

private fun calcHealersNeeded(creeps: List<Creep>): Int {
    val deficit = calcHealDeficit(creeps)
    if (deficit == 0) return 0
    return (deficit / HEAL_POWER).coerceAtLeast(1)
}

object BehaviorSelector {

    fun assignAll(gameplay: Gameplay) {
        assignCombatBehaviors(gameplay)
        assignSnakeBehaviors(gameplay)
    }

    // ── Harci csapat ─────────────────────────────────────────────────────────
    // Gyülekező ponton várnak amíg ellenség 20 range-en belülre nem ér
    // Utána támadnak, HYBRID healel ha kell

    private fun assignCombatBehaviors(gameplay: Gameplay) {
        val combatCreeps = gameplay.myCreeps.filter {
            it.role == Role.COMBAT_HYBRID || it.role == Role.COMBAT_RANGER
        }
        if (combatCreeps.isEmpty()) return

        val rallyPoint = gameplay.getCombatRallyPoint()
        val enemies = gameplay.getEnemyCreeps()
        val enemyClose = enemies.any { enemy ->
            combatCreeps.any { it.getRangeTo(enemy) <= 20 }
        }

        val healersNeeded = calcHealersNeeded(combatCreeps)
        var assignedHealers = 0

        for (creep in combatCreeps) {
            if (!enemyClose) {
                // Nincs ellenség közel → gyülekező pontra megy / vár
                val atRally = creep.getRangeTo(rallyPoint) <= 2
                creep.behavior = if (atRally) Behavior.WAIT else Behavior.CAPTURE
            } else {
                // Ellenség közel → támadás vagy heal
                if (creep.role == Role.COMBAT_HYBRID && assignedHealers < healersNeeded) {
                    creep.behavior = Behavior.HEAL
                    assignedHealers++
                } else {
                    creep.behavior = Behavior.FOCUS_FIRE
                }
            }
        }
    }

    // ── Kígyó ────────────────────────────────────────────────────────────────

    private fun assignSnakeBehaviors(gameplay: Gameplay) {
        val leader = SnakeManager.getLeader()
        val snakeCreeps = gameplay.myCreeps.filter { it.role == Role.SNAKE }

        for (creep in snakeCreeps) {
            creep.behavior = if (creep.id == leader?.id) Behavior.SNAKE_LEAD
            else Behavior.SNAKE_FOLLOW
        }
    }
}