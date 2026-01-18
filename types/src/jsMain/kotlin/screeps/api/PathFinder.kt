@file:JsModule("game/path-finder")
@file:JsNonModule

package screeps.api

import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally

@JsPlainObject
external interface SearchPathOptions {
    /** Custom navigation cost data. */
    var costMatrix: CostMatrix?

    /** Cost for walking on plain positions. The default is 2. */
    var plainCost: Int?

    /** Cost for walking on swamp positions. The default is 10. */
    var swampCost: Int?

    /**
     * Instead of searching for a path to the goals this will search for a path away from the goals.
     * The cheapest path that is out of range of every goal will be returned.
     * The default is false.
     */
    var flee: Boolean?

    /** The maximum allowed pathfinding operations. The default value is 50000. */
    var maxOps: Int?

    /** The maximum allowed cost of the path returned. The default is Infinity. */
    var maxCost: Int?

    /** Weight from 1 to 9 to apply to the heuristic in the A* formula F = G + weight * H. The default value is 1.2. */
    var heuristicWeight: Double?
}

@JsPlainObject
external interface SearchPathResult {
    /** The path found as an array of objects containing x and y properties. */
    var path: Array<Position>

    /** Total number of operations performed before this path was calculated. */
    var ops: Int

    /** The total cost of the path as derived from plainCost, swampCost, and given CostMatrix instance. */
    var cost: Int

    /** If the pathfinder fails to find a complete path, this will be true. */
    var incomplete: Boolean
}

/**
 * Container for custom navigation cost data.
 * If a non-0 value is found in the CostMatrix then that value will be used instead of the default terrain cost.
 */
external class CostMatrix {
    /**
     * Creates a new CostMatrix containing 0's for all positions.
     */
    constructor()

    /**
     * Get the cost of a position in this CostMatrix.
     * @param x The X position in the game.
     * @param y The Y position in the game.
     * @returns the cost at the specified position.
     */
    fun get(x: Int, y: Int): Int

    /**
     * Set the cost of a position in this CostMatrix.
     * @param x The X position in the game.
     * @param y The Y position in the game.
     * @param cost Cost of this position.
     */
    fun set(x: Int, y: Int, cost: Int)

    /**
     * @returns a new CostMatrix instance.
     */
    fun clone(): CostMatrix
}


/**
 * Find an optimal path between origin and goal.
 * @param origin The start position.
 * @param goal A goal or an array of goals.
 * @param options An object containing additional pathfinding flags.
 * @param options.costMatrix Custom navigation cost data.
 * @param options.plainCost Cost for walking on plain positions. The default is 2.
 * @param options.swampCost Cost for walking on swamp positions. The default is 10.
 * @param options.flee Instead of searching for a path to the goals this will search for a path away from the goals. The default is false.
 * @param options.maxOps The maximum allowed pathfinding operations. The default value is 50000.
 * @param options.maxCost The maximum allowed cost of the path returned. The default is Infinity.
 * @param options.heuristicWeight Weight from 1 to 9 to apply to the heuristic in the A* formula F = G + weight * H. The default value is 1.2.
 * @returns a SearchPathResult object with the search result.
 */
external fun searchPath(
    origin: Position,
    goal: dynamic,
    options: SearchPathOptions = definedExternally,
): SearchPathResult
