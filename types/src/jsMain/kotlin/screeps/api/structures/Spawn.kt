@file:JsModule("game/prototypes/spawn")
@file:JsNonModule

package screeps.api.structures

import kotlinx.js.JsPlainObject
import screeps.api.*

@JsPlainObject
external interface SpawnCreepResult {
    /** The instance of the {@link Creep} being spawned. */
    val `object`: Creep?

    /** The error code. */
    val error: ScreepsReturnCode?
}

/** Details of the creep being spawned currently. */
external open class Spawning {
    /** Time needed in total to complete the spawning. */
    val needTime: Int

    /** Remaining time to go. */
    val remainingTime: Int

    /** The creep that being spawned.
     *
     *  Due to a longstanding bug values from `Spawn.spawning.creep` have a different Prototype and
     *  different property types than a regular `screeps.api.Creep`.
     *  Use result of `Spawn.spawnCreep` instead.
     */
    val creep: SpawningCreep

    /** Cancel spawning immediately. */
    fun cancel(): ScreepsReturnCode?
}

/** This structure can create creeps. It also auto-regenerate a little amount of energy each tick. */
external open class StructureSpawn : OwnedStructure {
    /** A {@link Store} object that contains cargo of this structure. */
    val store: Store

    /** If the spawn is in process of spawning a new creep, this object will contain a {@link Spawning} object, or null otherwise. */
    val spawning: Spawning?

    /** The directions in which the spawn can create creeps. */
    val directions: Array<DirectionConstant>

    /**
     * Set the directions in which the spawn can create creeps.
     * @param directions An array of direction constants.
     */
    fun setDirections(directions: Array<DirectionConstant>): ScreepsReturnCode

    /**
     * Start the creep spawning process.
     * @param body An array describing the new creep’s body.
     * @returns a {@link SpawnCreepResult} object with the call result.
     */
    fun spawnCreep(body: Array<BodyPartType>): SpawnCreepResult
}

/**
 * Due to a longstanding bug values from `Spawn.spawning.creep` have a different Prototype and
 * different property types than a regular `screeps.api.Creep`.
 * Use result of `Spawn.spawnCreep` instead.
 */
external interface SpawningCreep {
    val id: Number // intentionally not int
    val body: Array<BodyPartDefinition>

    /** The maximum amount of hit points of the creep. */
    val hitsMax: Int

    /** Whether it is your creep. */
    val my: Boolean
}