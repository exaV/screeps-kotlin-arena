package season1.spawnstrike2.pipeline

import screeps.api.Creep
import season1.spawnstrike2.core.WorldContext

/** Egy creep egy tickben – stratégia lépés; új viselkedés = új osztály + szabály a láncban. */
fun interface CreepBehavior {
    fun tick(world: WorldContext, creep: Creep)
}
