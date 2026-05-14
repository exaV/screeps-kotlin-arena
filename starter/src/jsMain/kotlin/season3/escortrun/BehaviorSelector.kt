package season3.escortrun

import screeps.api.Creep
import screeps.api.getObjectsByPrototype
import season3.escortrun.combat.CombatTuning
import season3.escortrun.combat.SquadContact

/**
 * Harci viselkedés hozzárendelése. A szabályok szétválasztva:
 * - század kontakt: [SquadContact]
 * - távolságok: [CombatTuning]
 * - makró / VIP deny: [EscortRunStrategy]
 */
object BehaviorSelector {

    fun assignAll(gameplay: Gameplay) {
        assignCombatBehaviors(gameplay)
        assignSnakeBehaviors(gameplay)
    }

    private fun assignCombatBehaviors(gameplay: Gameplay) {
        val fightLine = gameplay.myCreeps.filter {
            it.role == Role.COMBAT_HYBRID ||
                it.role == Role.COMBAT_RANGER ||
                it.role == Role.COMBAT_FLAG_BLOCKER
        }
        val behaviorLine = gameplay.myCreeps.filter {
            it.role == Role.COMBAT_HYBRID || it.role == Role.COMBAT_RANGER
        }
        if (behaviorLine.isEmpty()) return

        val rallyPoint = gameplay.getCombatRallyPoint()
        val mapMid = gameplay.getMapCenterRally()

        val allHostiles = gameplay.getHostileCreeps()

        val globalFight = SquadContact.globalFightStance(
            gameplay, fightLine, allHostiles, rallyPoint, mapMid,
        )

        val combatAllies = gameplay.myCreeps.filter {
            it.role == Role.COMBAT_HYBRID ||
                it.role == Role.COMBAT_RANGER ||
                it.role == Role.COMBAT_FLAG_BLOCKER
        }
        val healRatio = CombatTuning.ALLY_HEAL_START_RATIO
        val needsHeal = combatAllies.any {
            it.hits < it.hitsMax * healRatio
        }
        var healerAssigned = false

        for (creep in behaviorLine) {
            val localThreat = allHostiles.any {
                creep.getRangeTo(it) <= CombatTuning.IMMEDIATE_THREAT_RANGE
            }
            val woundedAllyInHealRange = combatAllies.any { ally ->
                ally.id != creep.id &&
                    creep.getRangeTo(ally) <= CombatTuning.RANGED_ATTACK_RANGE &&
                    ally.hits < ally.hitsMax * healRatio
            }
            when {
                creep.role == Role.COMBAT_HYBRID &&
                    needsHeal &&
                    !healerAssigned &&
                    (!localThreat || woundedAllyInHealRange) -> {
                    creep.behavior = Behavior.HEAL
                    healerAssigned = true
                }
                localThreat || globalFight -> creep.behavior = Behavior.FOCUS_FIRE
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
