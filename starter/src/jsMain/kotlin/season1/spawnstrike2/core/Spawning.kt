package season1.spawnstrike2.core

import screeps.api.BodyPartType
import screeps.api.Creep
import screeps.api.structures.StructureSpawn

fun StructureSpawn.spawnFromParts(bodyParts: List<BodyPartType>): Creep? =
    spawnCreep(bodyParts.toTypedArray()).`object`
