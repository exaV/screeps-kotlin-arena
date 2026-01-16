@file:kotlin.js.JsModule("game/visual")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally

typealias Color = String
typealias LineStyle = String

typealias TextAlign = String

@JsPlainObject
external interface CircleVisualStyle {
    var radius: Double?
    var fill: Color?
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface LineVisualStyle {
    var width: Double?
    var color: Color?
    var opacity: Double?
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface PolyVisualStyle {
    var fill: Color?
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface RectVisualStyle {
    var fill: Color?
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface TextVisualStyle {
    var align: TextAlign?
    var backgroundColor: Color?
    var backgroundPadding: Double?
    var color: Color?
    var font: dynamic
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
}

external class Visual {
    constructor(layer: Double = definedExternally, persistent: Boolean = definedExternally)
    val layer: Double
    val persistent: Boolean
    fun clear(): Visual
    fun circle(position: Position, style: CircleVisualStyle = definedExternally): Visual
    fun line(pos1: Position, pos2: Position, style: LineVisualStyle = definedExternally): Visual
    fun poly(points: Array<Position>, style: PolyVisualStyle = definedExternally): Visual
    fun rect(pos: Position, w: Double, h: Double, style: RectVisualStyle = definedExternally): Visual
    fun size(): Double
    fun text(text: String, pos: Position, style: TextVisualStyle = definedExternally): Visual
}
