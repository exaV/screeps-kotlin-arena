package screeps.arena.api

@JsModule("game/prototypes/store")
@JsNonModule
external interface Store: Record<ResourceType, Double> {
    fun getCapacity(resource: ResourceType = definedExternally): Double?
    fun getUsedCapacity(resource: ResourceType = definedExternally): Double?
    fun getFreeCapacity(resource: ResourceType = definedExternally): Double?

}

// useful to compare entity.store[RESOURCE_ENERGY] < entity.store.getCapacity()
// without this it turns into entity.store[RESOURCE_ENERGY] ?: 0 < entity.store.getCapacity() ?: 0
operator fun Double?.compareTo(other: Double?): Int = compareValues(this ?: 0, other ?: 0.0)
operator fun Double?.compareTo(other: Int?): Int = compareValues(this ?: 0, other ?: 0.0)

