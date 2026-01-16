package screeps.arena.api

typealias Direction = DirectionConstant
typealias Terrain = TerrainConstant
typealias DoesZapCodeSpaceFlag = Int
typealias ResourceType = ResourceConstant
typealias BodyPartType = BodyPartConstant

inline val <T> Constant<T>.value: T get() = this.asDynamic().unsafeCast<T>()

// useful to compare entity.store[RESOURCE_ENERGY] < entity.store.getCapacity()
// without this it turns into entity.store[RESOURCE_ENERGY] ?: 0 < entity.store.getCapacity() ?: 0
operator fun Double?.compareTo(other: Double?): Int = compareValues(this ?: 0, other ?: 0.0)
operator fun Double?.compareTo(other: Int?): Int = compareValues(this ?: 0, other ?: 0)

val SpawnCreepResult.creep
    get() = `object`