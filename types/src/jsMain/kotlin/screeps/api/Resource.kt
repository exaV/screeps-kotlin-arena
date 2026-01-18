@file:JsModule("game/prototypes/resource")
@file:JsNonModule

package screeps.api

/** A dropped piece of resource. Dropped resource pile decays for ceil(amount/1000) units per tick. */
external open class Resource : GameObject {
    /** The amount of dropped resource. */
    val amount: Int
    /** The type of dropped resource (one of RESOURCE_* constants). */
    val resourceType: ResourceType
}
