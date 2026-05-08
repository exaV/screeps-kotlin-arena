package season1.spawnstrike

import sourcemaps.runWithSourceMapSupport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    runWithSourceMapSupport {
        SpawnStrike.tick()
    }
}

object SpawnStrike {

    private val gameplay = SpawnStrikeGameplay()

    fun tick() {

        // refresh myCreeps
        gameplay.refreshCreeps()

        // Spawn creep
        gameplay.spawnCreep()

        // add neutral point for creep
        gameplay.addNeutralPointToCreep()

        // select strategy for creep
        gameplay.selectStrategy()
    }
}