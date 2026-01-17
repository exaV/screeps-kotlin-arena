package season1.spawnstrike

import screeps.arena.api.arenaInfo

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    println("hello spawnstrike ${arenaInfo.season} - ${arenaInfo.name}")

}
