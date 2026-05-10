package season1.spawnstrike2.core

import screeps.api.getTicks

enum class GamePhase {
    EARLY,
    MIDDLE,
    LATE,
}

/** Játékfázis – küszöbök egy helyen; stratégiához igazítható. */
object SpawnStrikePhases {
    const val TICK_MIDDLE: Int = 270
    const val TICK_LATE: Int = 900

    fun current(): GamePhase = when {
        getTicks() > TICK_LATE -> GamePhase.LATE
        getTicks() > TICK_MIDDLE -> GamePhase.MIDDLE
        else -> GamePhase.EARLY
    }
}
