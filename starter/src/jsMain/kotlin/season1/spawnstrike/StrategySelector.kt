package season1.spawnstrike

import screeps.api.*

abstract class Selector(private val gameplay: SpawnStrikeGameplay) {
    protected var next: Selector? = null

    abstract fun select(creep: Creep): CreepStrategy

    fun setNext(selector: Selector): Selector {
        generateSequence(this) { it.next }.last().next = selector
        return this;
    }

    protected fun continueChain(creep: Creep): CreepStrategy {
        return next?.select(creep) ?: NeutralStrategy(gameplay)
    }

}

class CaptureMyFlagStrategySelector(private val gameplay: SpawnStrikeGameplay) : Selector(gameplay) {
    override fun select(creep: Creep): CreepStrategy {
        return if (
            getCurrentGamePhase() < GamePhase.MIDDLE &&
            creep.creepType == CreepType.CTF &&
            !isEnemyInRange(creep, gameplay.enemyCreeps, 5)
        ) {
            CaptureTheFlagStrategy(gameplay.myFlag)
        } else {
            continueChain(creep)
        }
    }
}

class CaptureTheFlagStrategySelector(private val gameplay: SpawnStrikeGameplay) : Selector(gameplay) {
    override fun select(creep: Creep): CreepStrategy {
        return if (getCurrentGamePhase() < GamePhase.MIDDLE && creep.creepType == CreepType.CTF) {
            CaptureTheFlagStrategy(gameplay.enemyFlag())
        } else {
            continueChain(creep)
        }
    }
}

class CaptureTheFlagEscortStrategySelector(private val gameplay: SpawnStrikeGameplay) : Selector(gameplay) {
    override fun select(creep: Creep): CreepStrategy {
        return if (getCurrentGamePhase() < GamePhase.MIDDLE /*&& creep.creepType == CreepType.CTF_ESCORT*/) {
            CaptureTheFlagEscortStrategy(gameplay)
        } else {
            continueChain(creep)
        }
    }
}

class AttackCreepStrategySelector(private val gameplay: SpawnStrikeGameplay) : Selector(gameplay) {
    override fun select(creep: Creep): CreepStrategy {
        return if (creepCanAttack(creep) && getCurrentGamePhase() >= GamePhase.LATE) {
            AttackCreepStrategy(gameplay)
        } else {
            continueChain(creep)
        }
    }
}

class LateGameStrategySelector(private val gameplay: SpawnStrikeGameplay) : Selector(gameplay) {
    override fun select(creep: Creep): CreepStrategy {
        return if (creepCanAttack(creep) && getCurrentGamePhase() >= GamePhase.LATE) {
            LateGameCreepStrategy(gameplay)
        } else {
            continueChain(creep)
        }
    }
}

class DefenderStrategySelector(private val gameplay: SpawnStrikeGameplay) : Selector(gameplay) {
    override fun select(creep: Creep): CreepStrategy {
        return if (
            creepCanAttack(creep) &&
            getCurrentGamePhase() < GamePhase.LATE &&
            isEnemyInRange(gameplay.mySpawn, gameplay.enemyCreeps, 15)
        ) {
            DefenderStrategy(gameplay)
        } else {
            continueChain(creep)
        }
    }
}

class HealCreepStrategySelector(private val gameplay: SpawnStrikeGameplay) : Selector(gameplay) {
    override fun select(creep: Creep): CreepStrategy {
        return if (
            creepCanHeal(creep) &&

            gameplay.myCreeps
                .filter { it.exists }
                .any { it.hits != it.hitsMax } &&

            (gameplay.myCreeps
                .filter { it.exists }
                .maxByOrNull { it.hits } == creep ||
                    gameplay.myCreeps
                        .filter { it.exists }
                        .size == 1)
        ) {
            println("pick heal strategy")
            HealCreepStrategy(gameplay)
        } else {
            continueChain(creep)
        }
    }

}