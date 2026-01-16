package screeps.arena.api

typealias Direction = DirectionConstant
typealias Terrain = TerrainConstant
typealias DoesZapCodeSpaceFlag = Int
typealias ResourceType = ResourceConstant
typealias BodyPartType = BodyPartConstant

inline val <T> Constant<T>.value: T get() = this.asDynamic().unsafeCast<T>()
