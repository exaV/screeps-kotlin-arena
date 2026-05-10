package season1.spawnstrike2.pipeline

import screeps.api.ATTACK
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.Flag
import screeps.api.GameObject
import screeps.api.MOVE
import screeps.api.Position
import screeps.api.RANGED_ATTACK
import screeps.api.findClosestByPath
import season1.spawnstrike2.core.WorldContext
import season1.spawnstrike2.core.canAttackBody
import season1.spawnstrike2.core.canHealBody
import season1.spawnstrike2.core.findClosestByRange
import season1.spawnstrike2.core.isEnemyInRange
import season1.spawnstrike2.core.spawnStrike2Kind
import season1.spawnstrike2.formation.gatheringCell

class DefenderBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        val closestEnemy = findClosestByRange(creep, world.enemyCreeps.toTypedArray())
        if (creep.attack(closestEnemy) == ERR_NOT_IN_RANGE) {
            creep.moveTo(closestEnemy)
        }
    }
}

class NeutralGatherBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        val cell = creep.gatheringCell(world) ?: return
        val moveToPosition = object : Position {
            override var x: Int = cell.x
            override var y: Int = cell.y
        }
        if (moveToPosition.x != 0 && moveToPosition.y != 0) {
            creep.moveTo(moveToPosition)
        }
    }
}

class AttackEnemyBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        val closestEnemy = findClosestByRange(creep, world.enemyCreeps.toTypedArray())
        val bodyTypes = creep.body.map { it.type }
        if (bodyTypes.contains(RANGED_ATTACK) && !bodyTypes.contains(ATTACK)) {
            if (creep.rangedAttack(closestEnemy) == ERR_NOT_IN_RANGE) {
                creep.moveTo(closestEnemy)
            }
        }
        if (creep.attack(closestEnemy) == ERR_NOT_IN_RANGE) {
            creep.moveTo(closestEnemy)
        }
        if (creep.attack(world.enemySpawn) == ERR_NOT_IN_RANGE) {
            creep.moveTo(world.enemySpawn)
        }
    }
}

class LateGameBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        var closestEnemy: GameObject = world.enemySpawn
        val closestEnemyCreep: GameObject = findClosestByRange(creep, world.enemyCreeps.toTypedArray())
        if (creep.getRangeTo(closestEnemyCreep) <= 10) {
            closestEnemy = closestEnemyCreep
        }
        val closestEnemyFlag = creep.findClosestByRange(world.enemyFlags().toTypedArray())
        val twoCreepsAlive = world.myCreeps.size == 2
        val lowestHealthFriendly = world.myCreeps
            .filter { it.exists }
            .filter { it.hits != it.hitsMax }
            .minByOrNull { it.hits }

        if (creep != lowestHealthFriendly && lowestHealthFriendly != null) {
            if (creep.heal(lowestHealthFriendly) == ERR_NOT_IN_RANGE) {
                creep.moveTo(lowestHealthFriendly)
                return
            } else {
                return
            }
        }

        if (creep.getRangeTo(closestEnemy) > 15 && world.enemyFlags().isNotEmpty() &&
            creep.getRangeTo(closestEnemyFlag) <= 15
        ) {
            findClosestByPath(creep, arrayOf(closestEnemyFlag))
                .also { creep.moveTo(it) }
            return
        }

        if (creep.rangedAttack(closestEnemy) == ERR_NOT_IN_RANGE) {
            if (twoCreepsAlive) {
                val twoCreepsInHealDistance = world.myCreeps[0].getRangeTo(world.myCreeps[1]) <= 1
                if (!twoCreepsInHealDistance) {
                    if (creep == world.myCreeps[0]) {
                        creep.moveTo(world.myCreeps[1])
                        return
                    } else {
                        creep.moveTo(world.myCreeps[0])
                        return
                    }
                }
            }
        }
        if (twoCreepsAlive && creep.hits > creep.hitsMax * 0.75 || !twoCreepsAlive) {
            creep.moveTo(closestEnemy)
        }
    }
}

class HealAlliesBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        val lowestHealthFriendly = world.myCreeps
            .filter { it.exists }
            .filter { it.hits != it.hitsMax }
            .filter { it.hitsMax > 200 }
            .minByOrNull { it.hits }

        if (lowestHealthFriendly?.hits != lowestHealthFriendly?.hitsMax) {
            lowestHealthFriendly?.let {
                if (creep.heal(lowestHealthFriendly) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(lowestHealthFriendly)
                }
            }
        } else {
            world.myCreeps
                .filter { it.exists }
                .maxByOrNull { it.hits }
                ?.let { creep.moveTo(it) }
        }
    }
}

class MoveToFlagBehavior(private val flag: Flag?) : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        flag ?: return
        findClosestByPath(creep, arrayOf(flag))
        creep.moveTo(flag)
    }
}

class MoveToMyFlagBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        MoveToFlagBehavior(world.myFlag).tick(world, creep)
    }
}

class MoveToEnemyFlagBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        MoveToFlagBehavior(world.enemyFlag()).tick(world, creep)
    }
}

class CtfEscortBehavior : CreepBehavior {
    override fun tick(world: WorldContext, creep: Creep) {
        if (creep.canAttackBody() && isEnemyInRange(creep, world.enemyCreeps, 10)) {
            world.refreshCreeps()
            val closestEnemy = findClosestByRange(creep, world.enemyCreeps.toTypedArray())
            if (creep.rangedAttack(closestEnemy) == ERR_NOT_IN_RANGE) {
                creep.moveTo(closestEnemy)
            }
            return
        }

        fun captureTheFlag(flag: Flag?) {
            flag ?: return
            findClosestByPath(creep, arrayOf(flag))
            creep.moveTo(flag)
        }

        val escortTarget = world.myCreeps.find { body ->
            body.body.map { it.type }.contains(MOVE) && body.body.size == 1
        }
        if (escortTarget != null) {
            val center = object : Position {
                override var x: Int = 0
                override var y: Int = 0
            }
            if (creep.getRangeTo(center) < 10) {
                captureTheFlag(world.enemyFlag())
            } else {
                captureTheFlag(world.myFlag)
            }
        } else {
            captureTheFlag(world.enemyFlag())
        }
    }
}
