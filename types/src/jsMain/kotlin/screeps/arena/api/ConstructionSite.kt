@file:kotlin.js.JsModule("game/prototypes/construction-site")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/** A site of a structure which is currently under construction. */
external open class ConstructionSite : GameObject {
    /** The current construction progress. */
    val progress: Int?
    /** The total construction progress needed for the structure to be built. */
    val progressTotal: Int?
    /** The structure that will be built (when the construction site is completed). */
    val structure: Structure?
    /** Whether it is your construction site. */
    val my: Boolean?

    /** Remove this construction site. */
    fun remove()
}
