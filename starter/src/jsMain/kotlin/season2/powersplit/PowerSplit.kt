package season2.powersplit

import sourcemaps.runWithSourceMapSupport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    runWithSourceMapSupport {
        powerSplit.tick()
    }
}

private val powerSplit = PowerSplit()

class PowerSplit {

    fun tick() {

    }

}