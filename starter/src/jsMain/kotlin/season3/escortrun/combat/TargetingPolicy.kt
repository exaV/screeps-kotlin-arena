package season3.escortrun.combat

import screeps.api.Creep
import season3.escortrun.Gameplay

/**
 * Célválasztás: saját EscortCreep-et sebző ellenség (stabil, legalacsonyabb HP), majd közeli fenyegetés,
 * végül [Gameplay.getPriorityTarget].
 */
object TargetingPolicy {

    fun selectAttackTarget(self: Creep, gameplay: Gameplay): Creep? {
        gameplay.getEscortDamageThreatPriority(self)?.let { return it }

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
