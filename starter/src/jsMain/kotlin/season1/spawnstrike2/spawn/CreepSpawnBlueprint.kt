package season1.spawnstrike2.spawn

import screeps.api.ATTACK
import screeps.api.BodyPartType
import screeps.api.HEAL
import screeps.api.MOVE
import screeps.api.RANGED_ATTACK
import screeps.api.structures.StructureSpawn
import screeps.api.Creep
import season1.spawnstrike2.core.SpawnStrike2CreepKind
import season1.spawnstrike2.core.spawnFromParts
import season1.spawnstrike2.core.spawnStrike2Kind

/** Egy creep típus spawnolása – új blueprint: implementáld ezt vagy add hozzá a listához. */
fun interface CreepBodyBlueprint {
    fun spawnAt(spawn: StructureSpawn): Creep?
}

class CtfRunnerBlueprint : CreepBodyBlueprint {
    override fun spawnAt(spawn: StructureSpawn): Creep? =
        spawn.spawnFromParts(listOf(MOVE))?.also { it.spawnStrike2Kind = SpawnStrike2CreepKind.CTF }
}

class CtfEscortBlueprint : CreepBodyBlueprint {
    override fun spawnAt(spawn: StructureSpawn): Creep? =
        spawn.spawnFromParts(listOf(RANGED_ATTACK, MOVE))?.also { it.spawnStrike2Kind = SpawnStrike2CreepKind.CTF_ESCORT }
}

class PowerfulBodyBlueprint : CreepBodyBlueprint {
    override fun spawnAt(spawn: StructureSpawn): Creep? =
        spawn.spawnFromParts(
            listOf(MOVE, MOVE, ATTACK, MOVE, ATTACK, MOVE, MOVE),
        )
}

class HealerBodyBlueprint : CreepBodyBlueprint {
    override fun spawnAt(spawn: StructureSpawn): Creep? =
        spawn.spawnFromParts(
            List(2) { MOVE } + List(2) { HEAL } + List(2) { MOVE } + List(2) { HEAL },
        )
}

/**
 * Gyári spawn sor – ugyanaz a sorrend / darabszám, mint a régi [season1.spawnstrike.SpawnStrikeGameplay.init]-ben.
 * Új hullám / test: itt másolj sort vagy adj hozzá [CreepBodyBlueprint] példányt.
 */
object CreepSpawnBlueprint {

    fun defaultFactories(): List<CreepBodyBlueprint> = buildList {
        addAll(List(1) { CtfRunnerBlueprint() })
        addAll(List(1) { CtfEscortBlueprint() })
        addAll(List(1) { CtfRunnerBlueprint() })
        addAll(List(1) { CtfEscortBlueprint() })
        addAll(List(5) { PowerfulBodyBlueprint() })
        addAll(List(1) { HealerBodyBlueprint() })
        addAll(List(20) { PowerfulBodyBlueprint() })
    }
}
