package season3.escortrun.combat

import screeps.api.Creep
import season3.escortrun.Gameplay

/**
 * Célválasztás: **közeli fenyegetés** mindig előrébb van, mint a stratégiai [Gameplay.getPriorityTarget].
 */
object TargetingPolicy {

    fun selectAttackTarget(self: Creep, gameplay: Gameplay): Creep? {
        val hostiles = gameplay.getHostileCreeps()
        if (hostiles.isEmpty()) return null

        val immediate = hostiles
            .filter { self.getRangeTo(it) <= CombatTuning.IMMEDIATE_THREAT_RANGE }
            .minWithOrNull(
                compareBy<Creep>({ self.getRangeTo(it) }, { it.hits }),
            )
        if (immediate != null) return immediate

        return gameplay.getPriorityTarget(self)
    }
}
