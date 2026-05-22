package season3.escortrun.economy

import screeps.api.*
import season3.escortrun.Gameplay
import season3.escortrun.pos

/**
 * Harvester (H1 / H2) viselkedés.
 *
 * **Boost mód** (!WorkerBehavior.boostedEconomyBuilt && boostEnabled):
 *   - H1 húzza W1-et a forrás mellé, majd H1+H2 együtt húzza W2-t a helyére
 *   - H2 segít W2 elhelyezésében
 *
 * **Normál mód** (WorkerBehavior.boostedEconomyBuilt || !boostEnabled):
 *
 *   H1 fix állomásokon ingázik (top):
 *     (4,4) → (5,5) → (6,6) → átadja H2-nek ha range ≤ 1 → vissza (4,4)
 *   H1 fix állomásokon ingázik (bot):
 *     (4,95) → (5,94) → (6,93) → átadja H2-nek ha range ≤ 1 → vissza
 *
 *   H2 két állomás között ugrál (top): (7,7) ↔ (8,8)
 *   H2 két állomás között ugrál (bot): (7,92) ↔ (8,91)
 *   Ha H1 range ≤ 1 → átveszi az energiát, majd a spawn felé adja át a lánc végén.
 *   H2 teli → spawn felé megy és átadja.
 */
object HarvesterBehavior {

    // ── Relay pozíciók ────────────────────────────────────────────────────────



    fun execute(creep: Creep, gameplay: Gameplay, boostEnabled: Boolean) {
        val h1   = EnergyChain.getPrimaryHarvester(gameplay)
        val isH1 = creep.id == h1?.id

        if (isH1) executeH1(creep, gameplay, boostEnabled)
        else      executeH2(creep, gameplay, boostEnabled)
    }

    // ── H1 ────────────────────────────────────────────────────────────────────

    private fun executeH1(creep: Creep, gameplay: Gameplay, boostEnabled: Boolean) {
        val boostedEconomyBuilt = WorkerBehavior.boostedEconomyBuilt

        val w1        = EnergyChain.getPrimaryWorker(gameplay)  ?: return
        val w2        = EnergyChain.getSecondaryWorker(gameplay)
        val h2        = EnergyChain.getSecondaryHarvester(gameplay)
        val positions = EscortPositions.get(gameplay.mySpawn.y)

        if (boostEnabled && !boostedEconomyBuilt) {
            // W1 még spawol → várakozás
            if (w1.spawning) {
                creep.moveTo(positions.waitingForWorker1)
                return
            }
            // W2 még spawol → ne álljunk meg, relay-ezzünk W1-gyel
            if (w2?.spawning == true) {
                executeNormalRelayH1(creep, gameplay, h2)
                return
            }
            // W1 nincs helyén → húzd oda
            if (!EnergyChain.isWorker1InPlace(gameplay)) {
                val w1Target = positions.worker1Target
                val jumpPos  = positions.harvester1JumpForW1
                creep.pull(w1)
                if (creep.x == w1Target.x && creep.y == w1Target.y) creep.moveTo(jumpPos)
                else creep.moveTo(w1Target)
                return
            }
            // W2 létezik, nincs helyén → H1 segít H2-vel W2 húzásában
            if (w2 != null && !w2.spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
                val w2Target = positions.worker2Target
                if (creep.getRangeTo(h2!!) > 1) {
                    creep.moveTo(positions.harvester1WaitingForWorker2)
                } else {
                    creep.pull(h2)
                    if (creep.x == w2Target.x && creep.y == w2Target.y) {
                        creep.pull(w1)
                        creep.moveTo(h2)
                    } else {
                        creep.moveTo(w2Target)
                    }
                }
                if (creep.x == w2Target.x && creep.y == w2Target.y) {
                    creep.pull(w1)
                    creep.moveTo(h2)
                }
                return
            }
        }

        executeNormalRelayH1(creep, gameplay, h2)
    }

    /**
     * H1 normál relay ingázás: W1-hez megy (átvesz), majd H2-nek / spawnnak átad.
     * W2 spawning közben is hívható.
     */
    private fun executeNormalRelayH1(creep: Creep, gameplay: Gameplay, h2: Creep?) {
        val pickupPos = if (gameplay.isTopSide) pos(4, 4) else pos(4, 95)

        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0) {
            // Üres → menj a fix pickup pozícióba, ott várj átvételre (W2 oldalán van a transfer)
            if (creep.x != pickupPos.x || creep.y != pickupPos.y) creep.moveTo(pickupPos)
        } else {
            // Teli → adj át H2-nek, vagy ha nincs H2, vidd a spawnhoz
            if (h2 != null) {
                if (creep.getRangeTo(h2) <= 1) creep.transfer(h2, RESOURCE_ENERGY)
                else creep.moveTo(h2)
            } else {
                val spawn = gameplay.mySpawn
                if (creep.getRangeTo(spawn) <= 1) creep.transfer(spawn, RESOURCE_ENERGY)
                else creep.moveTo(spawn)
            }
        }
    }

    // ── H2 ────────────────────────────────────────────────────────────────────

    private fun executeH2(creep: Creep, gameplay: Gameplay, boostEnabled: Boolean) {
        val boostedEconomyBuilt = WorkerBehavior.boostedEconomyBuilt

        val h1        = EnergyChain.getPrimaryHarvester(gameplay) ?: return
        val w2        = EnergyChain.getSecondaryWorker(gameplay)
        val positions = EscortPositions.get(gameplay.mySpawn.y)
        val spawn     = gameplay.mySpawn

        if (boostEnabled && !boostedEconomyBuilt) {
            // W2 még nincs helyén → H2 húzza W2-t
            if (w2 != null && !w2.spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
                val approachPos = positions.harvester1JumpForW2
                if (creep.getRangeTo(w2) > 1) {
                    creep.moveTo(w2)
                } else {
                    creep.pull(w2)
                    if (creep.x == approachPos.x && creep.y == approachPos.y) {
                        creep.pull(h1)
                        creep.moveTo(w2)
                    } else {
                        creep.moveTo(h1)
                    }
                }
                return
            }
        }

        // ── Normál relay ingázás ──────────────────────────────────────────────
        // H2 szabadon mozog: üres → H1-hez, teli → Spawn-hoz
        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0) {
            if (creep.getRangeTo(h1) <= 1) {
                // H1 átad ha van energiája – mi csak várunk
            } else {
                creep.moveTo(h1)
            }
        } else {
            if (creep.getRangeTo(spawn) <= 1) creep.transfer(spawn, RESOURCE_ENERGY)
            else creep.moveTo(spawn)
        }
    }
}

fun execute(creep: Creep, gameplay: Gameplay, boostEnabled: Boolean) {
    val h1   = EnergyChain.getPrimaryHarvester(gameplay)
    val isH1 = creep.id == h1?.id

    if (isH1) executeH1(creep, gameplay, boostEnabled)
    else      executeH2(creep, gameplay, boostEnabled)
}

// ── H1 ────────────────────────────────────────────────────────────────────

private fun executeH1(creep: Creep, gameplay: Gameplay, boostEnabled: Boolean) {
    val boostedEconomyBuilt = WorkerBehavior.boostedEconomyBuilt

    val w1        = EnergyChain.getPrimaryWorker(gameplay)  ?: return
    val w2        = EnergyChain.getSecondaryWorker(gameplay)
    val h2        = EnergyChain.getSecondaryHarvester(gameplay)
    val positions = EscortPositions.get(gameplay.mySpawn.y)

    if (boostEnabled && !boostedEconomyBuilt) {
        // W1 még spawol → várakozás
        if (w1.spawning) {
            creep.moveTo(positions.waitingForWorker1)
            return
        }
        // W2 spawol → várakozás
        if (w2?.spawning == true) {
            creep.moveTo(positions.harvester1WaitingForWorker2)
            return
        }
        // W1 nincs helyén → húzd oda
        if (!EnergyChain.isWorker1InPlace(gameplay)) {
            val w1Target = positions.worker1Target
            val jumpPos  = positions.harvester1JumpForW1
            creep.pull(w1)
            if (creep.x == w1Target.x && creep.y == w1Target.y) creep.moveTo(jumpPos)
            else creep.moveTo(w1Target)
            return
        }
        // W2 létezik, nincs helyén → H1 segít H2-vel W2 húzásában
        if (w2 != null && !w2.spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
            val w2Target = positions.worker2Target
            if (creep.getRangeTo(h2!!) > 1) {
                creep.moveTo(positions.harvester1WaitingForWorker2)
            } else {
                creep.pull(h2)
                if (creep.x == w2Target.x && creep.y == w2Target.y) {
                    creep.pull(w1)   // felülírjuk: W1-et húzzuk
                    creep.moveTo(h2)
                } else {
                    creep.moveTo(w2Target)
                }
            }
            if (creep.x == w2Target.x && creep.y == w2Target.y) {
                creep.pull(w1)
                creep.moveTo(h2)
            }
            return
        }
    }

    // Normál ingázás: üres → worker felé, teli → H2/Spawn felé
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0) {
        val target = if (boostedEconomyBuilt) w2 ?: w1 else w1
        if (creep.getRangeTo(target) > 1) creep.moveTo(target)
    } else {
        if (h2 != null) {
            if (creep.getRangeTo(h2) <= 1) creep.transfer(h2, RESOURCE_ENERGY)
            else creep.moveTo(h2)
        } else {
            val spawn = gameplay.mySpawn
            if (creep.getRangeTo(spawn) <= 1) creep.transfer(spawn, RESOURCE_ENERGY)
            else creep.moveTo(spawn)
        }
    }
}

// ── H2 ────────────────────────────────────────────────────────────────────

private fun executeH2(creep: Creep, gameplay: Gameplay, boostEnabled: Boolean) {
    val boostedEconomyBuilt = WorkerBehavior.boostedEconomyBuilt

    val h1        = EnergyChain.getPrimaryHarvester(gameplay) ?: return
    val w2        = EnergyChain.getSecondaryWorker(gameplay)
    val positions = EscortPositions.get(gameplay.mySpawn.y)
    val spawn     = gameplay.mySpawn

    if (boostEnabled && !boostedEconomyBuilt) {
        // W2 még nincs helyén → H2 húzza W2-t
        if (w2 != null && !w2.spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
            val approachPos = positions.harvester1JumpForW2
            if (creep.getRangeTo(w2) > 1) {
                creep.moveTo(w2)
            } else {
                creep.pull(w2)
                if (creep.x == approachPos.x && creep.y == approachPos.y) {
                    creep.pull(h1)
                    creep.moveTo(w2)
                } else {
                    creep.moveTo(h1)
                }
            }
            return
        }
    }

    // Normál ingázás: üres → H1 felé, teli → Spawn felé
    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) == 0) {
        if (creep.getRangeTo(h1) > 1) creep.moveTo(h1)
    } else {
        if (creep.getRangeTo(spawn) <= 1) creep.transfer(spawn, RESOURCE_ENERGY)
        else creep.moveTo(spawn)
    }
}