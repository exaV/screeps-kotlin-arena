package season1.portalexploration

import screeps.api.arenaInfo


@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    println("hello portalexploration ${arenaInfo.season} - ${arenaInfo.name}")

}
