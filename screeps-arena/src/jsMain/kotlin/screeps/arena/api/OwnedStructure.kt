@file:kotlin.js.JsModule("game/prototypes/owned-structure")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external abstract class OwnedStructure : Structure {
    val my: Boolean?
}
