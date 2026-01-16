@file:kotlin.js.JsModule("game/prototypes/structure")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external open class Structure : GameObject {
    val hits: Double?
    val hitsMax: Double?
}
