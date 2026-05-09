package season3.escortrun

import screeps.api.*
import screeps.api.structures.*
import season1.spawnstrike.spawnCreep

enum class CreepType {
    WORKER,
    HARVESTER,
    CARRIER,
    ATTACKER,
    HYBRID,      // MOVE, MOVE, RANGED_ATTACK, HEAL
    RANGER,      // MOVE, MOVE, MOVE, RANGED_ATTACK, RANGED_ATTACK, RANGED_ATTACK
    MOVE_ONLY,   // MOVE – kígyó tagja
    DEFENDER,
}

interface CreepFactory {
    fun createCreep(friendlySpawn: StructureSpawn): Creep?

    companion object {
        fun of(type: CreepType): CreepFactory = when (type) {
            CreepType.WORKER    -> Worker
            CreepType.HARVESTER -> Harvester
            CreepType.CARRIER   -> Carrier
            CreepType.ATTACKER  -> Attacker
            CreepType.HYBRID    -> Hybrid
            CreepType.RANGER    -> Ranger
            CreepType.MOVE_ONLY -> MoveOnly
            CreepType.DEFENDER  -> TODO()
        }
    }
}

sealed class Worker : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(WORK, WORK, WORK, CARRY))
    }
}

sealed class Harvester : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE, CARRY))
    }
}

sealed class Carrier : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE, MOVE, CARRY, CARRY))
    }
}

sealed class Attacker : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE, MOVE, RANGED_ATTACK, RANGED_ATTACK))
    }
}

sealed class Hybrid : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE, MOVE, RANGED_ATTACK, HEAL))
    }
}

sealed class Ranger : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE, MOVE, MOVE, RANGED_ATTACK, RANGED_ATTACK, RANGED_ATTACK))
    }
}

sealed class MoveOnly : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE))
    }
}