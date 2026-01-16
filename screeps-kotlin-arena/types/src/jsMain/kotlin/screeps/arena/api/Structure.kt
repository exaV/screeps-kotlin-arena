@file:kotlin.js.JsModule("game/prototypes/structure")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/** The base prototype object of all structures. */
external open class Structure : GameObject {
    /** The current amount of hit points of the structure. */
    val hits: Double?
    /** The maximum amount of hit points of the structure. */
    val hitsMax: Double?
}
