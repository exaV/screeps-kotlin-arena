package season2.ctf

import screeps.api.*
import sourcemaps.runWithSourceMapSupport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    runWithSourceMapSupport {
        CaptureTheFlag.tick()
    }
}

object CaptureTheFlag {

    val enemyFlag = getObjectsByPrototype(Flag::class).first { it.my == false }
    val myFlag = getObjectsByPrototype(Flag::class).first { it.my == true }


    fun tick() {
        val myCreeps = getObjectsByPrototype(Creep::class).filter { it.my && it.exists }

        for (creep in myCreeps) {
            creep.moveTo(enemyFlag)
        }

    }

}