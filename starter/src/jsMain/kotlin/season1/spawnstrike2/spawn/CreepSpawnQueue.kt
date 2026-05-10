package season1.spawnstrike2.spawn

import screeps.api.structures.StructureSpawn

class CreepSpawnQueue(
    initial: List<CreepBodyBlueprint>,
) {
    private val pending: MutableList<CreepBodyBlueprint> = initial.toMutableList()

    fun trySpawn(mySpawn: StructureSpawn) {
        if (mySpawn.spawning?.creep != null) return
        pending.removeFirstOrNull()?.spawnAt(mySpawn)
    }
}
