package screeps.arena.api

@JsModule("game/prototypes/store")
@JsNonModule
/** An object that class contain resources in its cargo. */
external interface Store: Record<ResourceType, Double> {
    /** Returns capacity of this store for the specified resource. */
    fun getCapacity(resource: ResourceType = definedExternally): Double?
    /** Returns free capacity for the store. */
    fun getUsedCapacity(resource: ResourceType = definedExternally): Double?
    /** Returns the capacity used by the specified resource. */
    fun getFreeCapacity(resource: ResourceType = definedExternally): Double?

}

// useful to compare entity.store[RESOURCE_ENERGY] < entity.store.getCapacity()
// without this it turns into entity.store[RESOURCE_ENERGY] ?: 0 < entity.store.getCapacity() ?: 0
operator fun Double?.compareTo(other: Double?): Int = compareValues(this ?: 0, other ?: 0.0)
operator fun Double?.compareTo(other: Int?): Int = compareValues(this ?: 0, other ?: 0.0)
