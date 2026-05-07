package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*
import sourcemaps.runWithSourceMapSupport

// Spawns
val spawns = getObjectsByPrototype(StructureSpawn::class.js)
val mySpawn = spawns.first { it.my == true }
val enemySpawn = spawns.first { it.my == false }

// Creeps
var myCreeps: MutableList<Creep> = mutableListOf()
var enemyCreeps: MutableList<Creep> = mutableListOf()

// Flags
var myFlag: Flag = getObjectsByPrototype(Flag::class).first { it.my == true }

// Extensions
val myExtensions: List<StructureExtension> = getObjectsByPrototype(StructureExtension::class).filter { it.my == true }


// Gameplay
var initialized = false
var creepFactories: MutableList<CreepFactory> = mutableListOf()

val topYValues = listOf(12, 13, 14)
val topXRange = 53..59
val bottomYValues = listOf(85, 86, 87)
val bottomXRange = 50..56

val points = mutableListOf<Point>()

val combat = fun(): Boolean {
    return mySpawn.findInRange(enemyCreeps.toTypedArray(), 20).isNotEmpty() || getTicks() > combatPhaseTicker
}
val enemyFlag = fun(): Flag? = getObjectsByPrototype(Flag::class).firstOrNull { flag -> flag.my == false }
val enemyFlags = fun(): List<Flag> = getObjectsByPrototype(Flag::class).filter { flag -> flag.my == false }
val flags = fun(): List<Flag> = getObjectsByPrototype(Flag::class).toList()

val center = object : Position {
    override var x: Int = 0
    override var y: Int = 0
}


fun initialize() {
    if (!initialized) {
        initialized = true

        creepFactories.add(FastCreepFactory())
        creepFactories.add(FastCreepEscortFactory())
        creepFactories.addAll(List(20) { PowerfulCreepFactory() })

        // Defender Zone
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
    }
}


@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {

    runWithSourceMapSupport {
        // init
        initialize()

        // tick
        SpawnStrike.tick()
    }
}

// Creep Strategies
interface CreepStrategy {
    fun doIt(creep: Creep);
}

class DefenderStrategy : CreepStrategy {
    override fun doIt(creep: Creep) {
        val closestEnemy = findClosestByRange(creep, enemyCreeps.toTypedArray())
        if (creep.attack(closestEnemy) == ERR_NOT_IN_RANGE) {
            creep.moveTo(closestEnemy);
        }
    }
}

class NeutralStrategy : CreepStrategy {
    override fun doIt(creep: Creep) {
        val moveToPosition = object : Position {
            override var x: Int = 0
            override var y: Int = 0
        }.apply {
            val gatheringPlace = creep.gatheringPlace()
            x = gatheringPlace?.x ?: 0
            y = gatheringPlace?.y ?: 0
        }

        takeIf { moveToPosition.x != 0 && moveToPosition.y != 0 }?.let { creep.moveTo(moveToPosition) }
    }
}

private const val combatPhaseTicker = 300

class AttackCreepStrategy : CreepStrategy {
    override fun doIt(creep: Creep) {

        val enemyCreeps = getObjectsByPrototype(Creep::class)
            .filter { it.exists }
            .filter { !it.my }

        if (getTicks() < combatPhaseTicker) {
            val closestEnemy = findClosestByRange(creep, enemyCreeps.toTypedArray())

            if (creep.body.map { it.type }.contains(RANGED_ATTACK) && !creep.body.map { it.type }.contains(ATTACK)) {
                if (creep.rangedAttack(closestEnemy) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(closestEnemy);
                }
            }

            if (creep.attack(closestEnemy) == ERR_NOT_IN_RANGE) {
                creep.moveTo(closestEnemy);
            }
        }


        if (creep.attack(enemySpawn) == ERR_NOT_IN_RANGE) {
            creep.moveTo(enemySpawn);
        }

    }
}

class HealCreepStrategy() : CreepStrategy {
    override fun doIt(creep: Creep) {
        val lowestHealthFriendly = myCreeps
            .filter { it.exists }
            .filter { it.hits != it.hitsMax }
            .minByOrNull { it.hits }

        if (lowestHealthFriendly?.hits != lowestHealthFriendly?.hitsMax) {
            lowestHealthFriendly?.let {
                if (creep.heal(lowestHealthFriendly) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(lowestHealthFriendly);
                }
            }
        } else {
            myCreeps
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

class CaptureTheFlagEscortStrategy : CreepStrategy {
    override fun doIt(creep: Creep) {

        if (creep.findInRange(enemyCreeps.toTypedArray(), 10).isNotEmpty() &&
            flags().any { it.findInRange(enemyCreeps.toTypedArray(), 4).isNotEmpty() }
        ) {
            AttackCreepStrategy().doIt(creep)
            return
        }

        if (flags().filter { it.my == true }.size == 2 || getTicks() > 350) {
            AttackCreepStrategy().doIt(creep)
            return
        }

        myCreeps.find { it.body.map { it.type }.contains(MOVE) && it.body.size == 1 }
            ?.let {
                if (it.x == myFlag.x && it.y == myFlag.y) {
                    val enemyFlag = getObjectsByPrototype(Flag::class).firstOrNull { flag -> flag.my == false }
                    println("Go to the another flag: ${enemyFlag?.x} - ${enemyFlag?.y}")
                    CaptureTheFlagStrategy(enemyFlag).doIt(creep)
                    return
                }

                creep.moveTo(it);
            } ?: CaptureTheFlagStrategy(enemyFlag()).doIt(creep)

    }
}


// Strategy selectors
abstract class Selector {
    protected var next: Selector? = null

    abstract fun select(creep: Creep): CreepStrategy

    fun setNext(selector: Selector): Selector {
        generateSequence(this) { it.next }.last().next = selector
        return this;
    }

    protected fun continueChain(creep: Creep): CreepStrategy {
        return next?.select(creep) ?: NeutralStrategy()
    }

}

class CaptureTheFlagStrategySelector : Selector() {
    override fun select(creep: Creep): CreepStrategy {
        return if (creep.body.map { it.type }.toList().size == 1) {
            CaptureTheFlagStrategy(enemyFlag())
        } else {
            continueChain(creep)
        }
    }
}

class CaptureMyFlagStrategySelector : Selector() {
    override fun select(creep: Creep): CreepStrategy {
        return if (creep.body.map { it.type }.toList().size == 1 && getTicks() < 150) {
            CaptureTheFlagStrategy(myFlag)
        } else {
            continueChain(creep)
        }
    }
}

class CaptureTheFlagEscortStrategySelector : Selector() {
    override fun select(creep: Creep): CreepStrategy {
        return if (creep.body.map { it.type }.toList().size == 2) {
            CaptureTheFlagEscortStrategy()
        } else {
            continueChain(creep)
        }
    }
}

class AttackCreepStrategySelector : Selector() {
    override fun select(creep: Creep): CreepStrategy {
        return if (getTicks() > 400 && mySpawn.findInRange(listOf(creep).toTypedArray(), 15).isNotEmpty() &&
            getTicks() > 900 && enemyCreeps.filter { it.exists }.isEmpty()
        ) {
            AttackCreepStrategy()
        } else if (isAttackerCreep(creep) && combat()) {
            AttackCreepStrategy()
        } else {
            continueChain(creep)

        }
    }
}

class DefenderStrategySelector : Selector() {
    override fun select(creep: Creep): CreepStrategy {
        return if (getTicks() > 400 && mySpawn.findInRange(listOf(creep).toTypedArray(), 15).isNotEmpty() &&
            mySpawn.findInRange(enemyCreeps.toTypedArray(), 15).isNotEmpty()
        ) {
            DefenderStrategy()
        } else {
            continueChain(creep)
        }
    }
}

class HealCreepStrategySelector : Selector() {
    override fun select(creep: Creep): CreepStrategy {
        return if (creep.body.map { it.type }.toList().contains(HEAL) && combat()) {
            HealCreepStrategy()
        } else {
            continueChain(creep)
        }
    }

}


// Creep Factory
interface CreepFactory {
    fun createCreep(friendlySpawn: StructureSpawn): Creep?
}

class FastCreepFactory : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep(listOf(MOVE))
    }
}

class FastCreepEscortFactory : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep(listOf(MOVE, RANGED_ATTACK))
    }
}

class PowerfulCreepFactory : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep(List(5) { MOVE } + List(3) { ATTACK })
    }
}

class HealerCreepFactory
    : CreepFactory {
    override fun createCreep(friendlySpawn: StructureSpawn): Creep? {
        return friendlySpawn.spawnCreep(List(5) { MOVE } + List(3) { HEAL })
    }
}

object SpawnStrike {
    fun tick() {

        // refresh myCreeps
        myCreeps = getObjectsByPrototype(Creep::class).filter { it.my }.toMutableList()

        // refresh enemyCreeps
        enemyCreeps = getObjectsByPrototype(Creep::class).filter { !it.my }.toMutableList()


        // Spawn creep
        takeIf { mySpawn.spawning?.creep == null }?.let {
            creepFactories.removeFirstOrNull()
                ?.createCreep(mySpawn)
        }


        // add neutral point to creep
        myCreeps.filter { it.exists }.filter { it.gatheringPlace() == null }.forEach { creep ->
            getNextFreePoint()?.let {
                it.creep = creep
            }
        }

        val strategySelectorChain =
            CaptureMyFlagStrategySelector().setNext(CaptureTheFlagStrategySelector())
                .setNext(CaptureTheFlagEscortStrategySelector())
                .setNext(AttackCreepStrategySelector())
                .setNext(DefenderStrategySelector())
                .setNext(HealCreepStrategySelector())


        myCreeps.filter { it.exists }.forEach { strategySelectorChain.select(it).doIt(it) }
    }
}

private fun isAttackerCreep(creep: Creep): Boolean = (creep.body.map { it.type }.toList().contains(RANGED_ATTACK) ||
        creep.body.map { it.type }.toList().contains(ATTACK))

// Basic functions
fun <T : Position> findClosestByRange(creep: Creep, positions: Array<T>): T {
    return creep.findClosestByRange(positions)
}


// Basic classes
data class Point(val x: Int, val y: Int) { // todo :Position
    var creep: Creep? = null
        get() = if (field?.exists == true) field else null
}

private fun getNextFreePoint(): Point? { // todo iterator
    return points.find { it.creep == null }
}


// Ext functions
private fun StructureSpawn.spawnCreep(bodyParts: List<BodyPartType>): Creep? {
    return this.spawnCreep(bodyParts.toTypedArray()).`object`
}

// Ext fields
private fun Creep.gatheringPlace() = points.find { it.creep == this }