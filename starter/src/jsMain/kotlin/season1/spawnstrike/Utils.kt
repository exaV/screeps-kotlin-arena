package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*

enum class GamePhase {
    EARLY,
    MIDDLE,
    LATE
}

fun getCurrentGamePhase(): GamePhase = if (getTicks() > 900) {
    GamePhase.LATE
} else if (getTicks() > 270) {
    GamePhase.MIDDLE
} else {
    GamePhase.EARLY
}

// Classes
data class Point(val x: Int, val y: Int) { // todo :Position
    var creep: Creep? = null
        get() = if (field?.exists == true) field else null
}

enum class CreepType {
    CTF,
    CTF_ESCORT,
    ALL_ROUNDER
}


// Functions
fun getNextFreePoint(gameplay: SpawnStrikeGameplay): Point? { // todo iterator
    return gameplay.points.find { it.creep == null }
}

fun creepCanAttack(creep: Creep): Boolean = (creep.body.map { it.type }.toList().contains(RANGED_ATTACK) ||
        creep.body.map { it.type }.toList().contains(ATTACK))

fun creepCanHeal(creep: Creep): Boolean = (creep.body.map { it.type }.toList().contains(HEAL))

fun getBodySize(creep: Creep): Int = creep.body.map { it.type }.toList().size


fun <T : Position> findClosestByRange(creep: Creep, positions: Array<T>): T = creep.findClosestByRange(positions)

fun isEnemyInRange(gameObject: GameObject, enemyCreeps: List<Creep>, range: Int): Boolean =
    gameObject.findInRange(enemyCreeps.toTypedArray(), range).isNotEmpty()


// Ext fields
private val creepTypeData = HashMap<Creep, CreepType>()
var Creep.creepType: CreepType
    get() = creepTypeData[this] ?: CreepType.ALL_ROUNDER
    set(value) {
        creepTypeData[this] = value
    }

// Ext functions
fun StructureSpawn.spawnCreep(bodyParts: List<BodyPartType>): Creep? {
    return this.spawnCreep(bodyParts.toTypedArray()).`object`
}

fun Creep.gatheringPlace(gameplay: SpawnStrikeGameplay) = gameplay.points.find { it.creep == this }