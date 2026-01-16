@file:kotlin.js.JsModule("game/prototypes/source")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external open class Source : GameObject {
    var energy: Double
    var energyCapacity: Double
}
