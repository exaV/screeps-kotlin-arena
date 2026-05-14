package season3.escortrun.combat

import screeps.api.Creep
import screeps.api.GameObject
import screeps.api.Position
import season3.escortrun.Behavior
import season3.escortrun.Gameplay
import season3.escortrun.Role
import season3.escortrun.behavior
import season3.escortrun.canAttack
import season3.escortrun.canHeal
import season3.escortrun.canRangedAttack
import season3.escortrun.role

/** Egy helyen: ranged / mass / közeledés / rally – a viselkedés enum alapján. */
object CombatExecutor {

    fun execute(creep: Creep, gameplay: Gameplay) {
        if (creep.role == Role.COMBAT_FLAG_BLOCKER) {
            executeEnemyFlagBlock(creep, gameplay)
            return
        }
        when (creep.behavior) {
            Behavior.WAIT -> holdOrAdvance(creep, gameplay, advanceToRally = false)
            Behavior.CAPTURE -> holdOrAdvance(creep, gameplay, advanceToRally = true)
            Behavior.FOCUS_FIRE -> focusFire(creep, gameplay)
            Behavior.HEAL -> healCombatLine(creep, gameplay)
            else -> {}
        }
    }

    /** Ellenséges capture cella: zászló vagy legközelebbi fő ellenséges spawn. */
    private fun enemyBlockGoal(gameplay: Gameplay): GameObject? {
        gameplay.getEnemyTeamFlag()?.let { return it }
        return gameplay.getEnemySpawns().minByOrNull { gameplay.mySpawn.getRangeTo(it) }
    }

    private fun executeEnemyFlagBlock(creep: Creep, gameplay: Gameplay) {
        val goal = enemyBlockGoal(gameplay) ?: return
        if (creep.x == goal.x && creep.y == goal.y) {
            val vip = gameplay.getEnemyEscortVip()
            if (vip != null && creep.canRangedAttack() && creep.getRangeTo(vip) <= CombatTuning.RANGED_ATTACK_RANGE) {
                creep.rangedAttack(vip)
            }
            return
        }
        creep.moveTo(goal)
    }

    private fun holdOrAdvance(creep: Creep, gameplay: Gameplay, advanceToRally: Boolean) {
        val rangedDone = tryRangedVolley(creep, gameplay)
        if (rangedDone) return

        val target = TargetingPolicy.selectAttackTarget(creep, gameplay)
        if (target != null && creep.getRangeTo(target) <= CombatTuning.HOLD_AND_RALLY_ENGAGE_RANGE) {
            creep.moveTo(target)
            return
        }
        if (advanceToRally) {
            val rally = gameplay.getCombatRallyPoint()
            if (creep.getRangeTo(rally) > 2) creep.moveTo(rally)
        }
    }

    private fun focusFire(creep: Creep, gameplay: Gameplay) {
        if (creep.canHeal()) {
            if (tryHealCombatAllies(creep, gameplay)) return
            val deferSelf = creep.hits >= creep.hitsMax * CombatTuning.SELF_HEAL_DEFER_TO_ALLY_ABOVE_RATIO
            if (!deferSelf && creep.hits < creep.hitsMax * CombatTuning.SELF_HEAL_HP_RATIO) {
                creep.heal(creep)
                return
            }
        }

        val target = TargetingPolicy.selectAttackTarget(creep, gameplay) ?: return
        val dist = creep.getRangeTo(target)

        val hostiles = gameplay.getHostileCreeps()
        val cluster = hostiles.count { creep.getRangeTo(it) <= CombatTuning.RANGED_ATTACK_RANGE }
        if (cluster >= 2 && creep.canRangedAttack()) {
            creep.rangedMassAttack()
            return
        }
        if (dist in 2..CombatTuning.RANGED_ATTACK_RANGE && creep.canRangedAttack()) {
            creep.rangedAttack(target)
            return
        }
        if (dist <= 1 && creep.canRangedAttack()) {
            creep.rangedAttack(target)
            moveAwayFromHostile(creep, target)
            return
        }
        if (dist <= 1 && creep.canAttack()) {
            creep.attack(target)
            moveAwayFromHostile(creep, target)
            return
        }
        if (dist > CombatTuning.RANGED_ATTACK_RANGE) {
            creep.moveTo(target)
        }
    }

    private fun healCombatLine(creep: Creep, gameplay: Gameplay) {
        if (tryHealCombatAllies(creep, gameplay)) return

        if (creep.canHeal() && creep.hits < creep.hitsMax * CombatTuning.SELF_HEAL_HP_RATIO) {
            creep.heal(creep)
            return
        }

        if (tryRangedVolley(creep, gameplay)) return

        val woundedCombat = gameplay.myCreeps
            .filter {
                (it.role == Role.COMBAT_HYBRID ||
                    it.role == Role.COMBAT_RANGER ||
                    it.role == Role.COMBAT_FLAG_BLOCKER) &&
                    it.hits < it.hitsMax * CombatTuning.ALLY_HEAL_HP_RATIO
            }
            .minByOrNull { it.hits }

        if (woundedCombat != null && creep.canHeal()) {
            val dist = creep.getRangeTo(woundedCombat)
            when {
                dist <= 1 -> creep.heal(woundedCombat)
                dist <= 3 -> creep.rangedHeal(woundedCombat)
                else -> creep.moveTo(woundedCombat)
            }
            return
        }
        holdOrAdvance(creep, gameplay, advanceToRally = false)
    }

    /** Közeli harcos társ heal – tick elején (FOCUS + HEAL). @return true ha heal lefutott */
    private fun tryHealCombatAllies(creep: Creep, gameplay: Gameplay): Boolean {
        if (!creep.canHeal()) return false
        val ratio = CombatTuning.ALLY_HEAL_START_RATIO
        val allies = gameplay.myCreeps.filter {
            it.id != creep.id &&
                (it.role == Role.COMBAT_HYBRID ||
                    it.role == Role.COMBAT_RANGER ||
                    it.role == Role.COMBAT_FLAG_BLOCKER) &&
                it.hits < it.hitsMax * ratio
        }
        val adjacent = allies.filter { creep.getRangeTo(it) <= 1 }.minByOrNull { it.hits }
        if (adjacent != null) {
            creep.heal(adjacent)
            return true
        }
        val ranged = allies.filter { creep.getRangeTo(it) in 2..CombatTuning.RANGED_ATTACK_RANGE }
            .minByOrNull { it.hits }
        if (ranged != null) {
            creep.rangedHeal(ranged)
            return true
        }
        return false
    }

    /** @return true ha ranged akció lefutott */
    private fun tryRangedVolley(creep: Creep, gameplay: Gameplay): Boolean {
        if (!creep.canRangedAttack()) return false
        val hostiles = gameplay.getHostileCreeps()
        val inRange = hostiles.filter { creep.getRangeTo(it) <= CombatTuning.RANGED_ATTACK_RANGE }
        if (inRange.isEmpty()) return false
        if (inRange.size >= 2) {
            creep.rangedMassAttack()
            return true
        }
        val pick = inRange.minByOrNull { it.hits }!!
        creep.rangedAttack(pick)
        return true
    }
}

private fun moveAwayFromHostile(creep: Creep, target: GameObject) {
    val dx = creep.x - target.x
    val dy = creep.y - target.y
    val escapeX = (creep.x + dx.coerceIn(-1, 1)).coerceIn(0, 99)
    val escapeY = (creep.y + dy.coerceIn(-1, 1)).coerceIn(0, 99)
    creep.moveTo(object : Position {
        override var x = escapeX
        override var y = escapeY
    })
}
