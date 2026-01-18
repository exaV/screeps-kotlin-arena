package tutorial

import screeps.api.arenaInfo

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    Init
    Tutorials10FinalTest.run()
}

object Init {
    init {
        println("running in ${arenaInfo.season} - ${arenaInfo.name}")
    }
}