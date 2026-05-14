package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*
import utils.Gameplay

class SpawnStrikeGameplay : Gameplay {
    val spawns: List<StructureSpawn> = getObjectsByPrototype(StructureSpawn::class.js).toList()
    val mySpawn: StructureSpawn = spawns.first { it.my == true }
    val enemySpawn: StructureSpawn = spawns.first { it.my == false }
    var myCreeps: MutableList<Creep> = mutableListOf()
    var enemyCreeps: MutableList<Creep> = mutableListOf()
    var myFlag: Flag = getObjectsByPrototype(Flag::class).first { it.my == true }
    var creepFactories: MutableList<CreepFactory> = mutableListOf()
    val enemyFlags: () -> List<Flag> = fun(): List<Flag> =
        getObjectsByPrototype(Flag::class).filter { flag -> flag.my == false }
    val points: MutableList<Point> = mutableListOf()
    val combat: () -> Boolean = fun(): Boolean {
        return mySpawn.findInRange(enemyCreeps.toTypedArray(), 20).isNotEmpty() || getTicks() > combatPhaseTicker
    }
    val enemyFlag: () -> Flag? = fun(): Flag? =
        getObjectsByPrototype(Flag::class).firstOrNull { flag -> flag.my == false }

    val flags: () -> List<Flag> = fun(): List<Flag> = getObjectsByPrototype(Flag::class).toList()
    var strategySelectorChain: Selector

    init {
        creepFactories.addAll(List(1) { CTFCreepFactory() })
        creepFactories.addAll(List(1) { CTFEscortCreepFactory() })
        creepFactories.addAll(List(1) { CTFCreepFactory() })
        creepFactories.addAll(List(1) { CTFEscortCreepFactory() })

//        creepFactories.addAll(List(2) { TestCreepFactory() })

        creepFactories.addAll(List(5) { PowerfulCreepFactory() })
        creepFactories.addAll(List(1) { HealerCreepFactory() })
        creepFactories.addAll(List(20) { PowerfulCreepFactory() })

        // Neutral Zone
        val topYValues: List<Int> = listOf(12, 13, 14)
        val topXRange: IntRange = 53..59
        val bottomYValues: List<Int> = listOf(85, 86, 87)
        val bottomXRange: IntRange = 50..56

        if (mySpawn.y > 50) {
            for (y in bottomYValues) {
                for (x in bottomXRange) {
                    points.add(Point(x, y))
                }
            }
        } else {
            for (y in topYValues) {
                for (x in topXRange) {
                    points.add(Point(x, y))
                }
            }
        }

        strategySelectorChain =
            CaptureMyFlagStrategySelector(this)
                .setNext(CaptureTheFlagStrategySelector(this))
                .setNext(CaptureTheFlagEscortStrategySelector(this))
                .setNext(DefenderStrategySelector(this))
                .setNext(HealCreepStrategySelector(this))
                .setNext(AttackCreepStrategySelector(this))

//        DefenderStrategySelector(this)
//            .setNext(HealCreepStrategySelector(this))
//            .setNext(CaptureTheFlagEscortStrategySelector(this))
//            .setNext(LateGameStrategySelector(this))
    }

    fun refreshCreeps() {
        myCreeps = getObjectsByPrototype(Creep::class).filter { it.my }.toMutableList()
        enemyCreeps = getObjectsByPrototype(Creep::class).filter { !it.my }.toMutableList()
    }

    fun spawnCreep() {
        takeIf { mySpawn.spawning?.creep == null }?.let {
            creepFactories.removeFirstOrNull()
                ?.createCreep(mySpawn)
        }
    }

    fun addNeutralPointToCreep() {
        myCreeps.filter { it.exists }.filter { it.gatheringPlace(this) == null }.forEach { creep ->
            getNextFreePoint(this)?.let {
                it.creep = creep
            }
        }
    }

    fun selectStrategy() {
        myCreeps.filter { it.exists }.forEach { strategySelectorChain.select(it).doIt(it) }
    }
}
