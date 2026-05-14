package season3.escortrun.combat

import screeps.api.Creep
import screeps.api.GameObject
import season3.escortrun.Gameplay
import season3.escortrun.Role
import season3.escortrun.canHeal
import season3.escortrun.canRangedAttack
import season3.escortrun.role

/**
 * Tickenként: ha már a VIP-et lövik (nincs más nem-VIP ellenség a harcosaink közelében), vagy csak a VIP maradt,
 * a **legközelebbi** ranged harcos megkapja a [Role.COMBAT_FLAG_BLOCKER] szerepet és az **ellenséges zászló**
 * (vagy fő ellenséges spawn) cellájára fut.
 */
object FlagBlockerAssigner {

    private fun blockGoal(gameplay: Gameplay): GameObject? {
        gameplay.getEnemyTeamFlag()?.let { return it }
        return gameplay.getEnemySpawns().minByOrNull { gameplay.mySpawn.getRangeTo(it) }
    }

    private fun restoreCombatRole(creep: Creep) {
        creep.role = if (creep.canHeal()) Role.COMBAT_HYBRID else Role.COMBAT_RANGER
    }

    fun update(gameplay: Gameplay) {
        for (c in gameplay.myCreeps.filter { it.exists && it.role == Role.COMBAT_FLAG_BLOCKER }) {
            restoreCombatRole(c)
        }
        if (!shouldAssignBlocker(gameplay)) return
        val goal = blockGoal(gameplay) ?: return
        val candidates = gameplay.myCreeps.filter { creep ->
            creep.exists &&
                creep.canRangedAttack() &&
                (creep.role == Role.COMBAT_HYBRID || creep.role == Role.COMBAT_RANGER)
        }
        if (candidates.isEmpty()) return
        val best = candidates.minWithOrNull(compareBy({ it.getRangeTo(goal) }, { it.id })) ?: return
        best.role = Role.COMBAT_FLAG_BLOCKER
    }

    private fun shouldAssignBlocker(gameplay: Gameplay): Boolean {
        if (gameplay.getEnemyEscortVip() == null) return false
        if (blockGoal(gameplay) == null) return false
        if (gameplay.getHostileCreeps().isEmpty()) return true

        val nonVipNearOurCombat = gameplay.getHostileCreeps().any { h ->
            gameplay.myCreeps.any { m ->
                (m.role == Role.COMBAT_HYBRID || m.role == Role.COMBAT_RANGER) &&
                    m.exists &&
                    m.getRangeTo(h) <= CombatTuning.IMMEDIATE_THREAT_RANGE
            }
        }
        if (nonVipNearOurCombat) return false

        val vip = gameplay.getEnemyEscortVip() ?: return false
        return gameplay.myCreeps.any { m ->
            m.exists &&
                (m.role == Role.COMBAT_HYBRID || m.role == Role.COMBAT_RANGER) &&
                m.canRangedAttack() &&
                m.getRangeTo(vip) <= CombatTuning.FLAG_BLOCKER_VIP_FOCUS_MAX_RANGE
        }
    }
}
