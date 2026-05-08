package season1.spawnstrike

import screeps.api.*

// Creep Strategies
interface CreepStrategy {
    fun doIt(creep: Creep);
}

class DefenderStrategy(private val gameplay: SpawnStrikeGameplay) : CreepStrategy {
    override fun doIt(creep: Creep) {
        val closestEnemy = findClosestByRange(creep, gameplay.enemyCreeps.toTypedArray())
        if (creep.attack(closestEnemy) == ERR_NOT_IN_RANGE) {
            creep.moveTo(closestEnemy);
        }
    }
}

class NeutralStrategy(private val gameplay: SpawnStrikeGameplay) : CreepStrategy {
    override fun doIt(creep: Creep) {
        val moveToPosition = object : Position {
            override var x: Int = 0
            override var y: Int = 0
        }.apply {
            val gatheringPlace = creep.gatheringPlace(gameplay)
            x = gatheringPlace?.x ?: 0
            y = gatheringPlace?.y ?: 0
        }

        takeIf { moveToPosition.x != 0 && moveToPosition.y != 0 }?.let { creep.moveTo(moveToPosition) }
    }
}

const val combatPhaseTicker = 300

class AttackCreepStrategy(private val gameplay: SpawnStrikeGameplay) : CreepStrategy {
    override fun doIt(creep: Creep) {

        val closestEnemy = findClosestByRange(creep, gameplay.enemyCreeps.toTypedArray())

        if (creep.body.map { it.type }.contains(RANGED_ATTACK) && !creep.body.map { it.type }.contains(ATTACK)) {
            if (creep.rangedAttack(closestEnemy) == ERR_NOT_IN_RANGE) {
                creep.moveTo(closestEnemy);
            }
        }

        if (creep.attack(closestEnemy) == ERR_NOT_IN_RANGE) {
            creep.moveTo(closestEnemy);
        }


        if (creep.attack(gameplay.enemySpawn) == ERR_NOT_IN_RANGE) {
            creep.moveTo(gameplay.enemySpawn);
        }

    }
}

class LateGameCreepStrategy(private val gameplay: SpawnStrikeGameplay) : CreepStrategy {
    override fun doIt(creep: Creep) {

        var closestEnemy: GameObject = gameplay.enemySpawn
        val closestEnemyCreep: GameObject = findClosestByRange(creep, gameplay.enemyCreeps.toTypedArray())
        if (creep.getRangeTo(closestEnemyCreep) <= 10) {
            closestEnemy = closestEnemyCreep
        }

        // todo ez ez próbálkozás de csak akkor ha a heal kommentelve van
//        gameplay.enemyCreeps
//            .filter { it.exists }
//            .filter { creep.getRangeTo(it) <= 10 }
//            .minByOrNull { it.hits }
//            ?.let { closestEnemy = it }


        val closestEnemyFlag = creep.findClosestByRange(gameplay.enemyFlags().toTypedArray())

        val twoCreepsAlive = gameplay.myCreeps.size == 2
        val lowestHealthFriendly = gameplay.myCreeps
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


        if (creep.getRangeTo(closestEnemy) > 15 && gameplay.enemyFlags().isNotEmpty() &&
            creep.getRangeTo(closestEnemyFlag) <= 15
        ) {
            findClosestByPath(creep, arrayOf(closestEnemyFlag))
                .also { flag -> creep.moveTo(flag) }
            return
        }


        if (creep.rangedAttack(closestEnemy) == ERR_NOT_IN_RANGE) {
            if (twoCreepsAlive) {
                val twoCreepsInHealDistance = gameplay.myCreeps[0].getRangeTo(gameplay.myCreeps[1]) <= 1
                if (!twoCreepsInHealDistance) {
                    if (creep == gameplay.myCreeps[0]) {
                        creep.moveTo(gameplay.myCreeps[1])
                        return
                    } else {
                        creep.moveTo(gameplay.myCreeps[0])
                        return
                    }
                }
            }
        }
        if (
            twoCreepsAlive && creep.hits > creep.hitsMax * 0.75 ||
            !twoCreepsAlive
        ) {
            creep.moveTo(closestEnemy)
        }
    }
}

class HealCreepStrategy(private val gameplay: SpawnStrikeGameplay) : CreepStrategy {
    override fun doIt(creep: Creep) {
        val lowestHealthFriendly = gameplay.myCreeps
            .filter { it.exists }
            .filter { it.hits != it.hitsMax }
            .filter { it.hitsMax > 200 }
            .minByOrNull { it.hits }

        if (lowestHealthFriendly?.hits != lowestHealthFriendly?.hitsMax) {
            lowestHealthFriendly?.let {
                if (creep.heal(lowestHealthFriendly) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(lowestHealthFriendly);
                }
            }
        } else {
            gameplay.myCreeps
                .filter { it.exists }
                .maxByOrNull { it.hits }
                ?.let { creep.moveTo(it) }
        }
    }

}

class CaptureTheFlagStrategy(private val flag: Flag?) : CreepStrategy {
    override fun doIt(creep: Creep) {
        flag?.let {
            findClosestByPath(creep, arrayOf(flag))
                .also { creep.moveTo(flag) }
        }
    }
}

class CaptureTheFlagEscortStrategy(private val gameplay: SpawnStrikeGameplay) : CreepStrategy {
    override fun doIt(creep: Creep) {

        if (
            creepCanAttack(creep) &&
            isEnemyInRange(creep, gameplay.enemyCreeps, 10)
//            &&
//            gameplay.flags().any { isEnemyInRange(it, gameplay.enemyCreeps, 4) }
        ) {
            gameplay.refreshCreeps()
            val closestEnemy = findClosestByRange(creep, gameplay.enemyCreeps.toTypedArray())

            if (creep.rangedAttack(closestEnemy) == ERR_NOT_IN_RANGE) {
                creep.moveTo(closestEnemy)
            }
            return
        }

        fun captureTheFlag(flag: Flag?) {
            flag?.let {
                findClosestByPath(creep, arrayOf(flag))
                    .also { creep.moveTo(flag) }
            }
            return
        }

        gameplay.myCreeps.find { it.body.map { it.type }.contains(MOVE) && it.body.size == 1 }
            ?.let {

                val center = object : Position {
                    override var x: Int = 0
                    override var y: Int = 0
                }

                if (creep.getRangeTo(center) < 10) {
                    captureTheFlag(gameplay.enemyFlag())
                } else {
                    captureTheFlag(gameplay.myFlag)
                }

            } ?: captureTheFlag(gameplay.enemyFlag())

    }
}