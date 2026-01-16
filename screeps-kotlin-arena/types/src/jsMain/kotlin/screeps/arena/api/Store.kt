@file:kotlin.js.JsModule("game/prototypes/store")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/** An object that class contain resources in its cargo. */
external interface Store: Record<ResourceType, Double> {
    /** Returns capacity of this store for the specified resource. */
    fun getCapacity(resource: ResourceType = definedExternally): Double?
    /** Returns free capacity for the store. */
    fun getUsedCapacity(resource: ResourceType = definedExternally): Double?
    /** Returns the capacity used by the specified resource. */
    fun getFreeCapacity(resource: ResourceType = definedExternally): Double?
}
