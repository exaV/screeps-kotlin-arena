@file:kotlin.js.JsModule("game/path-finder")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally

@JsPlainObject
external interface SearchPathOptions {
    var costMatrix: CostMatrix?
    var plainCost: Double?
    var swampCost: Double?
    var flee: Boolean?
    var maxOps: Double?
    var maxCost: Double?
    var heuristicWeight: Double?
}

@JsPlainObject
external interface SearchPathResult {
    var path: Array<Position>
    var ops: Double
    var cost: Double
    var incomplete: Boolean
}

external class CostMatrix {
    constructor()
    fun get(x: Double, y: Double): Double
    fun set(x: Double, y: Double, cost: Double)
    fun clone(): CostMatrix
}


external fun searchPath(
    origin: Position,
    goal: dynamic,
    options: SearchPathOptions = definedExternally,
): SearchPathResult
