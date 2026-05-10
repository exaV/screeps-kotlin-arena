package season1.spawnstrike2

import season1.spawnstrike2.core.WorldContext
import season1.spawnstrike2.formation.GatheringService
import season1.spawnstrike2.pipeline.SpawnStrike2StrategyRulebook
import sourcemaps.runWithSourceMapSupport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    runWithSourceMapSupport {
        SpawnStrike2Game.tick()
    }
}

private object SpawnStrike2Game {
    private val world = WorldContext.bootstrap()
    private val engine = SpawnStrike2StrategyRulebook.defaultEngine()

    fun tick() {
        world.refreshCreeps()
        world.spawnQueue.trySpawn(world.mySpawn)
        GatheringService(world).assignOpenSlots()
        engine.tick(world, world.myCreeps)
    }
}
