package season1.portalexploration

import screeps.arena.api.arenaInfo


@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    println("hello portalexploration ${arenaInfo.season} - ${arenaInfo.name}")

}
