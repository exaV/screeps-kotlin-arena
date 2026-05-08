package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*

interface CreepFactory {
    fun createCreep(friendlySpawn: StructureSpawn): Creep?
}

class CTFCreepFactory : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep(listOf(MOVE))
            .also { it?.creepType = CreepType.CTF }
    }
}

class CTFEscortCreepFactory : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep(listOf(RANGED_ATTACK, MOVE))
            .also { it?.creepType = CreepType.CTF_ESCORT }
    }
}

class PowerfulCreepFactory : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
//        return friendlySpawn.spawnCreep(List(5) { MOVE } + List(3) { ATTACK })
        return friendlySpawn.spawnCreep(listOf(MOVE, MOVE, ATTACK, MOVE, ATTACK, MOVE, MOVE))
    }
}

class HealerCreepFactory
    : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep(List(2) { MOVE } + List(2) { HEAL } + List(2) { MOVE } + List(2) { HEAL })
    }
}

class TestCreepFactory
    : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep((1..20).flatMap { listOf(RANGED_ATTACK, MOVE) } + MOVE + HEAL)
    }
}