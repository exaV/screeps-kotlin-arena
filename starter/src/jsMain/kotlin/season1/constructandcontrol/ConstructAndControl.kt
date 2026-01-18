package season1.constructandcontrol

import screeps.api.arenaInfo

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    println("hello constructandcontrol ${arenaInfo.season} - ${arenaInfo.name}")

}
