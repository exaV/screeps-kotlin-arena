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
    /** Circle radius, default is 0.15. */
    var radius: Double?
    /** Fill color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var fill: Color?
    /** Opacity value, default is 0.5. */
    var opacity: Double?
    /** Stroke color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var stroke: Color?
    /** Stroke line width, default is 0.1. */
    var strokeWidth: Double?
    /** Either undefined (solid line), dashed, or dotted. Default is undefined. */
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface LineVisualStyle {
    /** Line width, default is 0.1. */
    var width: Double?
    /** Line color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var color: Color?
    /** Opacity value, default is 0.5. */
    var opacity: Double?
    /** Either undefined (solid line), dashed, or dotted. Default is undefined. */
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface PolyVisualStyle {
    /** Fill color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var fill: Color?
    /** Opacity value, default is 0.5. */
    var opacity: Double?
    /** Stroke color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var stroke: Color?
    /** Stroke line width, default is 0.1. */
    var strokeWidth: Double?
    /** Either undefined (solid line), dashed, or dotted. Default is undefined. */
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface RectVisualStyle {
    /** Fill color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var fill: Color?
    /** Opacity value, default is 0.5. */
    var opacity: Double?
    /** Stroke color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var stroke: Color?
    /** Stroke line width, default is 0.1. */
    var strokeWidth: Double?
    /** Either undefined (solid line), dashed, or dotted. Default is undefined. */
    var lineStyle: LineStyle?
}

@JsPlainObject
external interface TextVisualStyle {
    /** Text align, either center, left, or right. Default is center. */
    var align: TextAlign?
    /**
     * Background color in the following format: #ffffff (hex triplet). Default is undefined (no background).
     * When background is enabled, text vertical align is set to middle (default is baseline).
     */
    var backgroundColor: Color?
    /** Background rectangle padding, default is 0.3. */
    var backgroundPadding: Double?
    /** Font color in the following format: #ffffff (hex triplet). Default is #ffffff. */
    var color: Color?
    /**
     * Either a number or a string in one of the following forms:
     * "0.7", "20px", "0.7 serif", or "bold italic 1.5 Times New Roman".
     */
    var font: dynamic
    /** Opacity value, default is 1. */
    var opacity: Double?
    /** Stroke color in the following format: #ffffff (hex triplet). default is undefined (no stroke). */
    var stroke: Color?
    /** Stroke line width, default is 0.15. */
    var strokeWidth: Double?
}

/**
 * Visuals provide a way to show various visual debug info in the game.
 * All draw coordinates are measured in game coordinates and centered to tile centers,
 * i.e. (10,10) will point to the center of the creep at x:10; y:10 position. Fractional coordinates are allowed.
 */
external class Visual {
    /**
     * Creates a new empty instance of {@link Visual}.
     * @param layer The layer of visuals in this object. Visuals of higher layer overlaps visuals of lower layer. Default is 0.
     * @param persistent Whether visuals in this object are persistent. Non-persistent visuals are visible during the current tick only.
     */
    constructor(layer: Double = definedExternally, persistent: Boolean = definedExternally)
    /** The layer of visuals in the object. */
    val layer: Double
    /** Whether visuals in this object are persistent. */
    val persistent: Boolean

    /**
     * Remove all visuals from the object.
     * @returns the {@link Visual} object itself, so that you can chain calls.
     */
    fun clear(): Visual

    /**
     * Draw a circle.
     * @param position The position object of the center. May be {@link GameObject} or any object containing x and y properties.
     * @param style An object with additional options.
     * @param style.radius Circle radius, default is 0.15.
     * @param style.fill Fill color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.opacity Opacity value, default is 0.5.
     * @param style.stroke Stroke color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.strokeWidth Stroke line width, default is 0.1.
     * @param style.lineStyle Either undefined (solid line), dashed, or dotted. Default is undefined.
     * @returns the {@link Visual} object itself, so that you can chain calls.
     */
    fun circle(position: Position, style: CircleVisualStyle = definedExternally): Visual

    /**
     * Draw a line.
     * @param pos1 The start position object. May be {@link GameObject} or any object containing x and y properties.
     * @param pos2 The finish position object. May be {@link GameObject} or any object containing x and y properties.
     * @param style An object with additional options.
     * @param style.width Line width, default is 0.1.
     * @param style.color Line color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.opacity Opacity value, default is 0.5.
     * @param style.lineStyle Either undefined (solid line), dashed, or dotted. Default is undefined.
     * @returns the {@link Visual} object itself, so that you can chain calls.
     */
    fun line(pos1: Position, pos2: Position, style: LineVisualStyle = definedExternally): Visual

    /**
     * Draw a polyline.
     * @param points An array of points. Every item may be {@link GameObject} or any object containing x and y properties.
     * @param style An object with additional options.
     * @param style.fill Fill color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.opacity Opacity value, default is 0.5.
     * @param style.stroke Stroke color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.strokeWidth Stroke line width, default is 0.1.
     * @param style.lineStyle Either undefined (solid line), dashed, or dotted. Default is undefined.
     * @returns the {@link Visual} object itself, so that you can chain calls.
     */
    fun poly(points: Array<Position>, style: PolyVisualStyle = definedExternally): Visual

    /**
     * Draw a rectangle.
     * @param pos The position object of the top-left corner. May be {@link GameObject} or any object containing x and y properties.
     * @param w The width of the rectangle.
     * @param h The height of the rectangle.
     * @param style An object with additional options.
     * @param style.fill Fill color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.opacity Opacity value, default is 0.5.
     * @param style.stroke Stroke color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.strokeWidth Stroke line width, default is 0.1.
     * @param style.lineStyle Either undefined (solid line), dashed, or dotted. Default is undefined.
     * @returns the {@link Visual} object itself, so that you can chain calls.
     */
    fun rect(pos: Position, w: Double, h: Double, style: RectVisualStyle = definedExternally): Visual

    /** @returns the size of the visuals in bytes. */
    fun size(): Double

    /**
     * Draw a text label. You can use any valid Unicode characters, including emoji.
     * @param text The text message.
     * @param pos The position object of the label baseline. May be {@link GameObject} or any object containing x and y properties.
     * @param style An object with additional options.
     * @param style.align Text align, either center, left, or right. Default is center.
     * @param style.backgroundColor Background color in the following format: #ffffff (hex triplet). Default is undefined (no background).
     * @param style.backgroundPadding Background rectangle padding, default is 0.3.
     * @param style.color Font color in the following format: #ffffff (hex triplet). Default is #ffffff.
     * @param style.font Either a number or a string in one of the following forms: "0.7", "20px", "0.7 serif", or "bold italic 1.5 Times New Roman".
     * @param style.opacity Opacity value, default is 1.
     * @param style.stroke Stroke color in the following format: #ffffff (hex triplet). default is undefined (no stroke).
     * @param style.strokeWidth Stroke line width, default is 0.15.
     * @returns the {@link Visual} object itself, so that you can chain calls.
     */
    fun text(text: String, pos: Position, style: TextVisualStyle = definedExternally): Visual
}
