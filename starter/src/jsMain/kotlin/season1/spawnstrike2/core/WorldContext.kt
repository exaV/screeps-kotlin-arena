package season1.spawnstrike2.core

import screeps.api.Creep
import screeps.api.Flag
import screeps.api.GameObject
import screeps.api.getObjectsByPrototype
import screeps.api.getTicks
import screeps.api.structures.StructureSpawn
import season1.spawnstrike2.formation.GatheringCell
import season1.spawnstrike2.formation.GatheringGridFactory
import season1.spawnstrike2.spawn.CreepSpawnBlueprint
import season1.spawnstrike2.spawn.CreepSpawnQueue

/**
 * Egy tickre frissített világállapot + állandók (spawn, zászló, gyűjtőrács).
 * Stratégiák csak ezt olvassák; új adat ehhez bővíthető.
 */
class WorldContext internal constructor(
    val mySpawn: StructureSpawn,
    val enemySpawn: StructureSpawn,
    val myFlag: Flag,
    val gatheringCells: MutableList<GatheringCell>,
    val spawnQueue: CreepSpawnQueue,
) {
    var myCreeps: MutableList<Creep> = mutableListOf()
    var enemyCreeps: MutableList<Creep> = mutableListOf()

    fun refreshCreeps() {
        myCreeps = getObjectsByPrototype(Creep::class).filter { it.my }.toMutableList()
        enemyCreeps = getObjectsByPrototype(Creep::class).filter { !it.my }.toMutableList()
    }

    fun enemyFlags(): List<Flag> =
        getObjectsByPrototype(Flag::class).filter { flag -> flag.my == false }

    fun enemyFlag(): Flag? =
        getObjectsByPrototype(Flag::class).firstOrNull { flag -> flag.my == false }

    fun allFlags(): List<Flag> = getObjectsByPrototype(Flag::class).toList()

    /** Régi [season1.spawnstrike.combatPhaseTicker] + spawn körüli ellenség. */
    fun inCombatPhase(): Boolean =
        mySpawn.findInRange(enemyCreeps.toTypedArray(), 20).isNotEmpty() ||
            getTicks() > COMBAT_PHASE_TICKER

    companion object {
        const val COMBAT_PHASE_TICKER: Int = 300

        fun bootstrap(): WorldContext {
            val spawns = getObjectsByPrototype(StructureSpawn::class.js).toList()
            val mySpawn = spawns.first { it.my == true }
            val enemySpawn = spawns.first { it.my == false }
            val myFlag = getObjectsByPrototype(Flag::class).first { it.my == true }
            val cells = GatheringGridFactory.buildCells(mySpawn.y)
            val queue = CreepSpawnQueue(CreepSpawnBlueprint.defaultFactories())
            return WorldContext(mySpawn, enemySpawn, myFlag, cells, queue)
        }
    }
}

fun <T : GameObject> findClosestByRange(creep: Creep, positions: Array<T>): T =
    creep.findClosestByRange(positions)

fun isEnemyInRange(gameObject: GameObject, enemyCreeps: List<Creep>, range: Int): Boolean =
    gameObject.findInRange(enemyCreeps.toTypedArray(), range).isNotEmpty()
