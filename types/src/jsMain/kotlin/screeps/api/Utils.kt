@file:JsModule("game/utils")
@file:JsNonModule

package screeps.api

import kotlin.js.JsClass
import kotlinx.js.JsPlainObject
import screeps.api.structures.Structure
import kotlin.js.definedExternally

@JsPlainObject
external interface HeapInfo {
    /** Total size of the heap. */
    var total_heap_size: Int
    /** Size of executable heap. */
    var total_heap_size_executable: Int
    /** Total physical size of the heap. */
    var total_physical_size: Int
    /** Total available size of the heap. */
    var total_available_size: Int
    /** Currently used heap size. */
    var used_heap_size: Int
    /** Heap size limit. */
    var heap_size_limit: Int
    /** Total malloced memory. */
    var malloced_memory: Int
    /** Peak malloced memory. */
    var peak_malloced_memory: Int
    /** Whether code space is zap garbage. */
    var does_zap_garbage: DoesZapCodeSpaceFlag
    /** Number of native contexts. */
    var number_of_native_contexts: Int
    /** Number of detached contexts. */
    var number_of_detached_contexts: Int
    /** Externally allocated size. */
    var externally_allocated_size: Int
}

@JsPlainObject
external interface FindPathOptions : SearchPathOptions {
    /** Objects which should not be treated as obstacles during the search. */
    var ignore: Array<out GameObject>?
}

/** {@link createConstructionSite} call result. */
@JsPlainObject
external interface CreateConstructionSiteResult {
    /** The instance of {@link ConstructionSite} created by this call. */
    val `object`: ConstructionSite?

    /** The error code. */
    val error: ScreepsReturnCode?
}

/**
 * Create new {@link ConstructionSite} at the specified location.
 * @param pos The location as an object with x and y properties.
 * @param structurePrototype A prototype that extends {@link GameObject}.
 * @returns a {@link CreateConstructionSiteResult} object with the call result.
 */
external fun <T : Structure> createConstructionSite(
    pos: Position,
    structurePrototype: JsClass<T>,
): CreateConstructionSiteResult

/**
 * Create new {@link ConstructionSite} at the specified location.
 * @param x The X coordinate.
 * @param y The Y coordinate.
 * @param structurePrototype A prototype that extends {@link GameObject}.
 * @returns a {@link CreateConstructionSiteResult} object with the call result.
 */
external fun <T : Structure> createConstructionSite(
    x: Int, y: Int,
    structurePrototype: JsClass<T>,
): CreateConstructionSiteResult


/**
 * Find a position with the shortest path from the given position.
 * @param fromPos The position to search from. May be {@link GameObject} or any object containing x and y properties.
 * @param positions The positions to search among. An array of {@link GameObject} or any objects containing x and y properties.
 * @param options An object containing additional pathfinding flags.
 * @param options.costMatrix Custom navigation cost data.
 * @param options.plainCost Cost for walking on plain positions. The default is 2.
 * @param options.swampCost Cost for walking on swamp positions. The default is 10.
 * @param options.flee Instead of searching for a path to the goals this will search for a path away from the goals. The default is false.
 * @param options.maxOps The maximum allowed pathfinding operations. The default value is 50000.
 * @param options.maxCost The maximum allowed cost of the path returned. The default is Infinity.
 * @param options.heuristicWeight Weight from 1 to 9 to apply to the heuristic in the A* formula F = G + weight * H. The default value is 1.2.
 * @param options.ignore objects which should not be treated as obstacles during the search.
 * @returns the closest object from {@link positions}, or null if there was no valid positions.
 */
external fun <T : Position> findClosestByPath(
    fromPos: Position,
    positions: Array<T>,
    options: FindPathOptions = definedExternally,
): T

/**
 * Find a position with the shortest linear distance from the given position.
 * @param fromPos The position to search from. May be {@link GameObject} or any object containing x and y properties.
 * @param positions The positions to search among. An array of {@link GameObject} or any objects containing x and y properties.
 * @returns the closest object from {@link positions}.
 */
external fun <T : Position> findClosestByRange(
    fromPos: Position,
    positions: Array<T>,
): T

/**
 * Find all objects in the specified linear range.
 * @param fromPos The origin position. May be {@link GameObject} or any object containing x and y properties.
 * @param positions The positions to search. An array of {@link GameObject} or any objects containing x and y properties.
 * @param range The range distance.
 * @returns an array with the objects found.
 */
external fun <T : Position> findInRange(
    fromPos: Position,
    positions: Array<T>,
    range: Int,
): Array<T>

/**
 * Find an optimal path between fromPos and toPos.
 * Unlike {@link searchPath}, findPath avoid all obstacles by default (unless costMatrix is specified).
 * @param fromPos The start position. May be {@link GameObject} or any object containing x and y properties.
 * @param toPos The target position. May be {@link GameObject} or any object containing x and y properties.
 * @param options An object containing additional pathfinding flags.
 * @param options.costMatrix Custom navigation cost data.
 * @param options.plainCost Cost for walking on plain positions. The default is 2.
 * @param options.swampCost Cost for walking on swamp positions. The default is 10.
 * @param options.flee Instead of searching for a path to the goals this will search for a path away from the goals. The default is false.
 * @param options.maxOps The maximum allowed pathfinding operations. The default value is 50000.
 * @param options.maxCost The maximum allowed cost of the path returned. The default is Infinity.
 * @param options.heuristicWeight Weight from 1 to 9 to apply to the heuristic in the A* formula F = G + weight * H. The default value is 1.2.
 * @param options.ignore objects which should not be treated as obstacles during the search.
 * @returns the path found as an array of objects containing x and y properties.
 */
external fun findPath(
    fromPos: Position,
    toPos: Position,
    options: FindPathOptions = definedExternally,
): Array<Position>

/**
 * @returns CPU wall time elapsed in the current tick in nanoseconds.
 */
external fun getCpuTime(): Int

/**
 * Get linear direction by differences of x and y.
 * @param dx The difference of X coordinate.
 * @param dy The difference of Y coordinate.
 * @returns a number representing one of the direction constants.
 */
external fun getDirection(dx: Int, dy: Int): Direction

/**
 * Use this method to get heap statistics for your virtual machine.
 */
external fun getHeapStatistics(): HeapInfo

/**
 * Get an object with the specified unique ID.
 * @param id The id property of the needed object. See {@link GameObject} prototype.
 */
external fun getObjectById(id: String): GameObject

/**
 * Get all game objects in the game.
 */
external fun getObjects(): Array<GameObject>

/**
 * Get all objects in the game with the specified prototype, for example, all creeps.
 * @param prototype A prototype that extends {@link GameObject}.
 * @returns Array of objects with the specified prototype.
 */
external fun <T : GameObject> getObjectsByPrototype(
    prototype: JsClass<T>,
): Array<T>

/**
 * Get linear range between two objects. a and b may be {@link GameObject} or any object containing x and y properties.
 * @param a The first of two objects. May be {@link GameObject} or any object containing x and y properties.
 * @param b The second of two objects. May be {@link GameObject} or any object containing x and y properties.
 * @returns a number of squares between two objects.
 */
external fun getRange(a: Position, b: Position): Int

/**
 * Get an integer representation of the terrain at the given position.
 * @param pos The position as an object containing x and y properties.
 * @returns either {@link TERRAIN_PLAIN}, {@link TERRAIN_WALL}, or {@link TERRAIN_SWAMP}.
 */
external fun getTerrainAt(pos: Position): Terrain

/**
 * @returns the number of ticks passed from the start of the current game.
 */
external fun getTicks(): Int
