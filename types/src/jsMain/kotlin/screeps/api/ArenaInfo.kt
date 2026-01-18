@file:JsModule("game")
@file:JsNonModule

package screeps.api

external interface ArenaInfo {
    val cpuTimeLimit: Int
    val cpuTimeLimitFirstTick: Int
    val level: Int
    val name: String
    val season: String?
    val ticksLimit: Int
}

external val arenaInfo: ArenaInfo