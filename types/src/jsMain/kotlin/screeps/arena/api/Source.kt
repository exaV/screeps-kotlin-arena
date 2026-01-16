@file:kotlin.js.JsModule("game/prototypes/source")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/** An energy source object. Can be harvested by creeps with a {@link WORK} body part. */
external open class Source : GameObject {
    /** Current amount of energy in the source. */
    val energy: Double
    /** The maximum amount of energy in the source. */
    val energyCapacity: Double
}
