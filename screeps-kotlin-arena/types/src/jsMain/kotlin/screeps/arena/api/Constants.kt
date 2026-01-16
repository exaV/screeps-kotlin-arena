@file:kotlin.js.JsModule("game/constants")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external val OK: Int
external val ERR_NOT_OWNER: Int
external val ERR_NO_PATH: Int
external val ERR_NAME_EXISTS: Int
external val ERR_BUSY: Int
external val ERR_NOT_FOUND: Int
external val ERR_NOT_ENOUGH_ENERGY: Int
external val ERR_NOT_ENOUGH_RESOURCES: Int
external val ERR_INVALID_TARGET: Int
external val ERR_FULL: Int
external val ERR_NOT_IN_RANGE: Int
external val ERR_INVALID_ARGS: Int
external val ERR_TIRED: Int
external val ERR_NO_BODYPART: Int
external val ERR_NOT_ENOUGH_EXTENSIONS: Int

external val MOVE: String
external val RANGED_ATTACK: String
external val HEAL: String
external val ATTACK: String
external val CARRY: String
external val TOUGH: String
external val WORK: String

external val TOP: Int
external val TOP_RIGHT: Int
external val RIGHT: Int
external val BOTTOM_RIGHT: Int
external val BOTTOM: Int
external val BOTTOM_LEFT: Int
external val LEFT: Int
external val TOP_LEFT: Int

external val TERRAIN_PLAIN: Int
external val TERRAIN_WALL: Int
external val TERRAIN_SWAMP: Int

external val BODYPART_HITS: Int

external val RANGED_ATTACK_POWER: Int
external val RANGED_ATTACK_DISTANCE_RATE: dynamic
external val ATTACK_POWER: Int
external val HEAL_POWER: Int
external val RANGED_HEAL_POWER: Int
external val CARRY_CAPACITY: Int
external val REPAIR_POWER: Int
external val DISMANTLE_POWER: Int
external val REPAIR_COST: Double
external val DISMANTLE_COST: Double
external val HARVEST_POWER: Int
external val BUILD_POWER: Int

external val OBSTACLE_OBJECT_TYPES: dynamic

external val TOWER_ENERGY_COST: Int
external val TOWER_RANGE: Int
external val TOWER_HITS: Int
external val TOWER_CAPACITY: Int
external val TOWER_POWER_ATTACK: Int
external val TOWER_POWER_HEAL: Int
external val TOWER_POWER_REPAIR: Int
external val TOWER_OPTIMAL_RANGE: Int
external val TOWER_FALLOFF_RANGE: Int
external val TOWER_FALLOFF: Double
external val TOWER_COOLDOWN: Int

external val BODYPART_COST: dynamic

external val MAX_CREEP_SIZE: Int
external val CREEP_SPAWN_TIME: Int

external val RESOURCE_ENERGY: String
external val RESOURCES_ALL: dynamic

external val SOURCE_ENERGY_REGEN: Int

external val RESOURCE_DECAY: Int

external val MAX_CONSTRUCTION_SITES: Int

external val CONSTRUCTION_COST: dynamic
external val STRUCTURE_PROTOTYPES: dynamic

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
