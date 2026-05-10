package season1.spawnstrike2.formation

import screeps.api.Creep
import season1.spawnstrike2.core.WorldContext

class GatheringService(private val world: WorldContext) {

    fun assignOpenSlots() {
        world.myCreeps.filter { it.exists }.filter { it.gatheringCell(world) == null }.forEach { creep ->
            nextFreeCell(world)?.assignedCreep = creep
        }
    }

    private fun nextFreeCell(world: WorldContext): GatheringCell? =
        world.gatheringCells.find { it.assignedCreep == null }
}

fun Creep.gatheringCell(world: WorldContext): GatheringCell? =
    world.gatheringCells.find { it.assignedCreep?.id == this.id }
