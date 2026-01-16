@file:kotlin.js.JsModule("game/prototypes/owned-structure")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/** The base prototype for a structure that has an owner. */
external abstract class OwnedStructure : Structure {
    /** true for your structure, false for a hostile structure, undefined for a neutral structure. */
    val my: Boolean?
}
