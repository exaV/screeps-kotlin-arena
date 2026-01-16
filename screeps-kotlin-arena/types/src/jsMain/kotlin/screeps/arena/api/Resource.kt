@file:kotlin.js.JsModule("game/prototypes/resource")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/** A dropped piece of resource. Dropped resource pile decays for ceil(amount/1000) units per tick. */
external open class Resource : GameObject {
    /** The amount of dropped resource. */
    val amount: Double
    /** The type of dropped resource (one of RESOURCE_* constants). */
    val resourceType: ResourceType
}
