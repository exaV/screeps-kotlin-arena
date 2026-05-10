package season3.escortrun

import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.RESOURCE_ENERGY
import screeps.api.getObjectsByPrototype
import screeps.api.getRange
import screeps.api.structures.StructureContainer

/**
 * MOVE×2 WORK×2 CARRY: hub → konténer withdraw → saját ConstructionSite build → proxy spawn transfer.
 * Hub: [Gameplay.expansionHubPosition].
 */
object ExpansionExecution {

    private const val HUB_CONTAINER_SEARCH_RANGE: Int = 4

    fun execute(creep: Creep, gameplay: Gameplay) {
        val hub = gameplay.expansionHubPosition()
        val container = findOurContainerNearHub(hub) ?: run {
            if (creep.getRangeTo(hub) > 1) creep.moveTo(hub)
            return
        }

        if (creep.getRangeTo(container) > 1) {
            creep.moveTo(container)
            return
        }

        creep.withdraw(container, RESOURCE_ENERGY)

        val energy = creep.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0
        val site = findMyConstructionSiteNear(creep)
        if (site != null && energy > 0) {
            creep.build(site)
            return
        }

        for (ps in gameplay.getMySpawns()) {
            if (ps.id == gameplay.mySpawn.id) continue
            if (creep.getRangeTo(ps) <= 1 && (creep.store.getUsedCapacity() ?: 0) > 0) {
                creep.transfer(ps, RESOURCE_ENERGY)
                return
            }
        }
    }

    private fun findMyConstructionSiteNear(creep: Creep): ConstructionSite? =
        getObjectsByPrototype(ConstructionSite::class.js).toList()
            .filter { it.exists && it.my == true && creep.getRangeTo(it) <= 3 }
            .minByOrNull { creep.getRangeTo(it) }

    private fun findOurContainerNearHub(hub: screeps.api.Position): StructureContainer? =
        getObjectsByPrototype(StructureContainer::class.js).toList()
            .filter { it.exists && it.my != false && getRange(hub, it) <= HUB_CONTAINER_SEARCH_RANGE }
            .minByOrNull { getRange(hub, it) }
}
