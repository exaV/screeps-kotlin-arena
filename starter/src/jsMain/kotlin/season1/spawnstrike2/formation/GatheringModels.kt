package season1.spawnstrike2.formation

import screeps.api.Creep

/** Semleges zóna egy cellája – ide gyűjtünk MOVE creepet. */
data class GatheringCell(val x: Int, val y: Int) {
    var assignedCreep: Creep? = null
        get() = if (field?.exists == true) field else null
}
