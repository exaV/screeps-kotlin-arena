package season1.spawnstrike

import screeps.api.arenaInfo

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    println("hello spawnstrike ${arenaInfo.season} - ${arenaInfo.name}")
}
