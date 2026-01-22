package tutorial

import screeps.api.arenaInfo
import sourcemaps.runWithSourceMapSupport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    runWithSourceMapSupport {
        Init
        Tutorials10FinalTest.run()
    }
}

object Init {
    init {
        println("running in ${arenaInfo.season} - ${arenaInfo.name}")
    }
}