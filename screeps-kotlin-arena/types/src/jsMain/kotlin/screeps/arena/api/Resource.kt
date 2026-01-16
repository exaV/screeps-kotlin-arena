@file:kotlin.js.JsModule("game/prototypes/resource")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external open class Resource : GameObject {
    var amount: Double
    var resourceType: ResourceType
}
