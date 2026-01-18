@file:JsModule("game/prototypes/structure")
@file:JsNonModule

package screeps.api.structures

import screeps.api.GameObject

/** The base prototype object of all structures. */
external open class Structure : GameObject {
    /** The current amount of hit points of the structure. */
    val hits: Int?
    /** The maximum amount of hit points of the structure. */
    val hitsMax: Int?
}
