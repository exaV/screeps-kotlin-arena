package season3.escortrun

import screeps.api.Creep
import screeps.api.HEAL_POWER
import screeps.api.getObjectsByPrototype

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

    private fun assignCombatBehaviors(gameplay: Gameplay) {
        val combatCreeps = gameplay.myCreeps.filter {
            it.role == Role.COMBAT_HYBRID || it.role == Role.COMBAT_RANGER
        }
        if (combatCreeps.isEmpty()) return

        val rallyPoint = gameplay.getCombatRallyPoint()

        val allEnemies = getObjectsByPrototype(screeps.api.Creep::class.js).toList()
            .filter { it.exists && it.my == false }

        // Ellenség közel ha bármely combat creeptől <= 20, VAGY ha bárki látótávolságon belül
        val enemyClose = allEnemies.any { enemy ->
            combatCreeps.any { it.getRangeTo(enemy) <= 20 }
        }

        val woundedAllies = gameplay.myCreeps.filter { it.hits < it.hitsMax * 0.85 }
        val needsHeal = woundedAllies.isNotEmpty()
        var healerAssigned = false

        for (creep in combatCreeps) {
            when {
                // Van ellenség közel → azonnal támad, nem nézi a rally pozíciót
                enemyClose -> {
                    if (creep.role == Role.COMBAT_HYBRID && needsHeal && !healerAssigned) {
                        creep.behavior = Behavior.HEAL
                        healerAssigned = true
                    } else {
                        creep.behavior = Behavior.FOCUS_FIRE
                    }
                }
                // Nincs ellenség → rally felé megy vagy vár
                creep.getRangeTo(rallyPoint) <= 3 -> creep.behavior = Behavior.WAIT
                else -> creep.behavior = Behavior.CAPTURE
            }
        }
    }

    private fun assignSnakeBehaviors(gameplay: Gameplay) {
        val leader = SnakeManager.getLeader()
        val snakeCreeps = gameplay.myCreeps.filter { it.role == Role.SNAKE }

        for (creep in snakeCreeps) {
            creep.behavior = if (creep.id == leader?.id) Behavior.SNAKE_LEAD
            else Behavior.SNAKE_FOLLOW
        }
    }
}