@file:kotlin.js.JsModule("game")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external interface ArenaInfo {
    val cpuTimeLimit: Int
    val cpuTimeLimitFirstTick: Int
    val level: Int
    val name: String
    val season: String?
    val ticksLimit: Int
}

external val arenaInfo: ArenaInfo