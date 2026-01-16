@file:kotlin.js.JsModule("game/prototypes/spawn")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface SpawnCreepResult {
    var `object`: Creep?
    var error: ScreepsReturnCode?
}

external open class Spawning {
    var needTime: Double
    var remainingTime: Double
    var creep: Creep
    fun cancel(): ScreepsReturnCode?
}

external open class StructureSpawn : OwnedStructure {
    var store: Store
    var spawning: Spawning?
    var directions: Array<DirectionConstant>
    fun setDirections(directions: Array<DirectionConstant>): ScreepsReturnCode
    fun spawnCreep(body: Array<BodyPartType>): SpawnCreepResult
}
