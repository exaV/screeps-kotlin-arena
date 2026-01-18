@file:JsModule("game/prototypes/store")
@file:JsNonModule

package screeps.api

/** An object that class contain resources in its cargo. */
external interface Store: Record<ResourceType, Int> {
    /** Returns capacity of this store for the specified resource. */
    fun getCapacity(resource: ResourceType = definedExternally): Int?
    /** Returns free capacity for the store. */
    fun getUsedCapacity(resource: ResourceType = definedExternally): Int?
    /** Returns the capacity used by the specified resource. */
    fun getFreeCapacity(resource: ResourceType = definedExternally): Int?
}
