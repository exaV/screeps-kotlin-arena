@file:kotlin.js.JsModule("game/prototypes/spawn")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject

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

    /** The creep that being spawned. */
    val creep: Creep

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
