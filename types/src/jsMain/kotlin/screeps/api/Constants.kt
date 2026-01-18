@file:JsModule("game/constants")
@file:JsNonModule

package screeps.api

external interface Constant<T>


typealias StringConstant = Constant<String>
typealias IntConstant = Constant<Int>

sealed external interface ScreepsReturnCode : IntConstant
external interface BodyPartConstant : StringConstant
external interface DirectionConstant : IntConstant
external interface TerrainConstant : IntConstant
external interface ResourceConstant : StringConstant

external object OK : ScreepsReturnCode
external object ERR_NOT_OWNER : ScreepsReturnCode
external object ERR_NO_PATH : ScreepsReturnCode
external object ERR_NAME_EXISTS : ScreepsReturnCode
external object ERR_BUSY : ScreepsReturnCode
external object ERR_NOT_FOUND : ScreepsReturnCode
external object ERR_NOT_ENOUGH_ENERGY : ScreepsReturnCode
external object ERR_NOT_ENOUGH_RESOURCES : ScreepsReturnCode
external object ERR_INVALID_TARGET : ScreepsReturnCode
external object ERR_FULL : ScreepsReturnCode
external object ERR_NOT_IN_RANGE : ScreepsReturnCode
external object ERR_INVALID_ARGS : ScreepsReturnCode
external object ERR_TIRED : ScreepsReturnCode
external object ERR_NO_BODYPART : ScreepsReturnCode
external object ERR_NOT_ENOUGH_EXTENSIONS : ScreepsReturnCode

external val MOVE: BodyPartConstant
external val RANGED_ATTACK: BodyPartConstant
external val HEAL: BodyPartConstant
external val ATTACK: BodyPartConstant
external val CARRY: BodyPartConstant
external val TOUGH: BodyPartConstant
external val WORK: BodyPartConstant

external val TOP: DirectionConstant
external val TOP_RIGHT: DirectionConstant
external val RIGHT: DirectionConstant
external val BOTTOM_RIGHT: DirectionConstant
external val BOTTOM: DirectionConstant
external val BOTTOM_LEFT: DirectionConstant
external val LEFT: DirectionConstant
external val TOP_LEFT: DirectionConstant

external val TERRAIN_PLAIN: TerrainConstant
external val TERRAIN_WALL: TerrainConstant
external val TERRAIN_SWAMP: TerrainConstant

external val BODYPART_HITS: Int

external val RANGED_ATTACK_POWER: Int
external val RANGED_ATTACK_DISTANCE_RATE: Record<Int, Int>
external val ATTACK_POWER: Int
external val HEAL_POWER: Int
external val RANGED_HEAL_POWER: Int
external val CARRY_CAPACITY: Int
external val REPAIR_POWER: Int
external val DISMANTLE_POWER: Int
external val REPAIR_COST: Int
external val DISMANTLE_COST: Int
external val HARVEST_POWER: Int
external val BUILD_POWER: Int

external val OBSTACLE_OBJECT_TYPES: Array<String>

external val TOWER_ENERGY_COST: Int
external val TOWER_RANGE: Int
external val TOWER_HITS: Int
external val TOWER_CAPACITY: Int
external val TOWER_POWER_ATTACK: Int
external val TOWER_POWER_HEAL: Int
external val TOWER_POWER_REPAIR: Int
external val TOWER_OPTIMAL_RANGE: Int
external val TOWER_FALLOFF_RANGE: Int
external val TOWER_FALLOFF: Int
external val TOWER_COOLDOWN: Int

external val BODYPART_COST: Record<BodyPartConstant, Int>

external val MAX_CREEP_SIZE: Int
external val CREEP_SPAWN_TIME: Int

external val RESOURCE_ENERGY: ResourceConstant
external val RESOURCES_ALL: Array<ResourceConstant>

external val SOURCE_ENERGY_REGEN: Int

external val RESOURCE_DECAY: Int

external val MAX_CONSTRUCTION_SITES: Int

external val CONSTRUCTION_COST: Record<String, Int>
external val STRUCTURE_PROTOTYPES: Record<String, Any>

external val CONSTRUCTION_COST_ROAD_SWAMP_RATIO: Int
external val CONSTRUCTION_COST_ROAD_WALL_RATIO: Int

external val CONTAINER_HITS: Int
external val CONTAINER_CAPACITY: Int

external val WALL_HITS: Int
external val WALL_HITS_MAX: Int

external val RAMPART_HITS: Int
external val RAMPART_HITS_MAX: Int

external val ROAD_HITS: Int
external val ROAD_WEAROUT: Int

external val EXTENSION_HITS: Int
external val EXTENSION_ENERGY_CAPACITY: Int

external val SPAWN_ENERGY_CAPACITY: Int
external val SPAWN_HITS: Int
