@file:kotlin.js.JsModule("game/prototypes/game-object")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally


external interface Position {
    var x: Double
    var y: Double
}

@JsPlainObject
external interface EffectData {
    var multiplier: Double
}

external class Effect {
    var effectType: String
    var data: EffectData
}

external open class GameObject : Position {
    var exists: Boolean
    var id: dynamic
    var ticksToDecay: Double?
    override var x: Double
    override var y: Double
    var effects: Array<Effect>?

    fun <T : Position> findClosestByPath(
        positions: Array<T>,
        options: FindPathOptions = definedExternally,
    ): T

    fun <T : Position> findClosestByRange(positions: Array<T>): T

    fun <T : Position> findInRange(positions: Array<T>, range: Double): Array<T>

    fun findPathTo(pos: Position, options: FindPathOptions = definedExternally): Array<Position>

    fun getRangeTo(pos: Position): Double
}
