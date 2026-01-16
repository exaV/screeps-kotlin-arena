@file:kotlin.js.JsModule("game/utils")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlin.js.JsClass
import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally

@JsPlainObject
external interface HeapInfo {
    var total_heap_size: Double
    var total_heap_size_executable: Double
    var total_physical_size: Double
    var total_available_size: Double
    var used_heap_size: Double
    var heap_size_limit: Double
    var malloced_memory: Double
    var peak_malloced_memory: Double
    var does_zap_garbage: DoesZapCodeSpaceFlag
    var number_of_native_contexts: Double
    var number_of_detached_contexts: Double
    var externally_allocated_size: Double
}

@JsPlainObject
external interface FindPathOptions : SearchPathOptions {
    var ignore: Array<GameObject>?
}

@JsPlainObject
external interface CreateConstructionSiteResult {
    var `object`: ConstructionSite?
    var error: ScreepsReturnCode?
}

external fun <T : Structure> createConstructionSite(
    pos: Position,
    structurePrototype: JsClass<T>,
): CreateConstructionSiteResult

external fun <T : Structure> createConstructionSite(
    x: Double, y: Double,
    structurePrototype: JsClass<T>,
): CreateConstructionSiteResult


external fun <T : Position> findClosestByPath(
    fromPos: Position,
    positions: Array<T>,
    options: FindPathOptions = definedExternally,
): T

external fun <T : Position> findClosestByRange(
    fromPos: Position,
    positions: Array<T>,
): T

external fun <T : Position> findInRange(
    fromPos: Position,
    positions: Array<T>,
    range: Double,
): Array<T>

external fun findPath(
    fromPos: Position,
    toPos: Position,
    options: FindPathOptions = definedExternally,
): Array<Position>

external fun getCpuTime(): Double

external fun getDirection(dx: Double, dy: Double): Direction

external fun getHeapStatistics(): HeapInfo

external fun getObjectById(id: dynamic): GameObject

external fun getObjects(): Array<GameObject>

external fun <T : GameObject> getObjectsByPrototype(
    prototype: JsClass<T>,
): Array<T>

external fun getRange(a: Position, b: Position): Double

external fun getTerrainAt(pos: Position): Terrain

external fun getTicks(): Double
