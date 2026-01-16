@file:kotlin.js.JsModule("game/prototypes/construction-site")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external open class ConstructionSite : GameObject {
    val progress: Double?
    val progressTotal: Double?
    val structure: Structure?
    val my: Boolean?
    fun remove()
}
