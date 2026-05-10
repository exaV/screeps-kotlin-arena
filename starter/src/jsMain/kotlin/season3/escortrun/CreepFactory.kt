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
    /** Korai opener: gyors, heal nélkül. */
    SKIRMISHER,  // MOVE, MOVE, RANGED_ATTACK
    /** Korai opener: távoli konténer + második spawn. */
    EXPANSION_RUNNER, // MOVE, MOVE, WORK, WORK, CARRY
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
            CreepType.HYBRID            -> Hybrid
            CreepType.RANGER           -> Ranger
            CreepType.SKIRMISHER       -> Skirmisher
            CreepType.EXPANSION_RUNNER -> ExpansionRunner
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

sealed class Skirmisher : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE, MOVE, RANGED_ATTACK))
    }
}

sealed class ExpansionRunner : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE, MOVE, WORK, WORK, CARRY))
    }
}

sealed class MoveOnly : CreepFactory {
    companion object : CreepFactory {
        override fun createCreep(friendlySpawn: StructureSpawn): Creep? =
            friendlySpawn.spawnCreep(listOf(MOVE))
    }
}