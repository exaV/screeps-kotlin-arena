package season3.escortrun

import screeps.api.Creep
import screeps.api.Position

// ── EnergyChain ───────────────────────────────────────────────────────────────
//
// Fix koordináták (EscortRun játékmód):
//
//   Fent:  spawn(9,9),  source(2,2)  → Worker1(3,3), Worker2(3,2)
//          Harvester1 jump: (3,2)→(3,3) W1-hez, (3,3)→(3,2)... TODO finomítás
//
//   Lent:  spawn(9,90), source(2,97) → Worker1(3,96), Worker2(3,97)
//
// Lánc:
//   1W+1H: Source → W1(fix) → H1(ingázik W1↔Spawn)
//   2W+2H: Source → W2(fix) → W1(fix) → H1(ingázik W1↔H2) → H2(ingázik H1↔Spawn)

// Fix pozíció helper
private fun pos(x: Int, y: Int): Position = object : Position {
    override var x = x
    override var y = y
}

// ── Pozíció konstansok ────────────────────────────────────────────────────────

data class ChainPositions(
    val worker1Target: Position,       // Worker1 végső helye
    val worker2Target: Position,       // Worker2 végső helye
    val harvester1JumpForW1: Position, // H1 jump pozíció Worker1 húzásához
    val harvester1JumpForW2: Position, // H1 jump pozíció Worker2 húzásához
)

object EscortPositions {
    // Spawn Y koordinátájából döntjük el fent/lent
    fun get(spawnY: Int): ChainPositions = if (spawnY < 50) {
        // FENT: spawn(9,9), source(2,2)
        // W2 spawol: 8,8
        // H1 húzza W2-t átlósan majd egyenesen:
        //   H1 út: 8,8→7,7→6,6→5,5→4,4→4,3→3,2→4,2
        //   jumpPos = 4,2 (H1 végső pozíció, W2 ekkor 3,2-re kerül)
        ChainPositions(
            worker1Target       = pos(3, 3),
            worker2Target       = pos(3, 2),
            harvester1JumpForW1 = pos(4, 3),
            harvester1JumpForW2 = pos(4, 2),
        )
    } else {
        // LENT: spawn(9,90), source(2,97)
        // W2 spawol: 8,91
        // jumpPos tükrözve
        ChainPositions(
            worker1Target       = pos(3, 96),
            worker2Target       = pos(3, 97),
            harvester1JumpForW1 = pos(4, 96),
            harvester1JumpForW2 = pos(4, 97),
        )
    }
}

// ── EnergyChain ──────────────────────────────────────────────────────────────

object EnergyChain {

    fun getSortedWorkers(gameplay: Gameplay): List<Creep> =
        gameplay.myCreeps.filter { it.role == Role.WORKER }.sortedBy { it.id }

    fun getSortedHarvesters(gameplay: Gameplay): List<Creep> =
        gameplay.myCreeps.filter { it.role == Role.HARVESTER }.sortedBy { it.id }

    fun getPrimaryWorker(gameplay: Gameplay): Creep? =
        getSortedWorkers(gameplay).firstOrNull()

    fun getSecondaryWorker(gameplay: Gameplay): Creep? =
        getSortedWorkers(gameplay).getOrNull(1)

    fun getPrimaryHarvester(gameplay: Gameplay): Creep? =
        getSortedHarvesters(gameplay).firstOrNull()

    fun getSecondaryHarvester(gameplay: Gameplay): Creep? =
        getSortedHarvesters(gameplay).getOrNull(1)

    // Worker1 a célpozícióján van-e (pontosan)
    fun isWorker1InPlace(gameplay: Gameplay): Boolean {
        val w1 = getPrimaryWorker(gameplay) ?: return false
        val target = EscortPositions.get(gameplay.mySpawn.y).worker1Target
        return w1.x == target.x && w1.y == target.y
    }

    // Worker2 a célpozícióján van-e (pontosan)
    fun isWorker2InPlace(gameplay: Gameplay): Boolean {
        val w2 = getSecondaryWorker(gameplay) ?: return false
        val target = EscortPositions.get(gameplay.mySpawn.y).worker2Target
        return w2.x == target.x && w2.y == target.y
    }
}