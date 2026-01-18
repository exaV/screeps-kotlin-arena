@file:JsModule("game/prototypes/source")
@file:JsNonModule

package screeps.api

/** An energy source object. Can be harvested by creeps with a {@link WORK} body part. */
external open class Source : GameObject {
    /** Current amount of energy in the source. */
    val energy: Int
    /** The maximum amount of energy in the source. */
    val energyCapacity: Int
}
