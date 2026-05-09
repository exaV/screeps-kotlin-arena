package season3.escortrun

import screeps.api.*
import screeps.api.season3.EscortCreep
import screeps.api.structures.*
import season3.escortrun.GameplayUtil.getMyCreeps
import season3.escortrun.GameplayUtil.getMyEscortCreep
import season3.escortrun.GameplayUtil.getMySource
import season3.escortrun.GameplayUtil.getMySpawn

// ── Utilities ─────────────────────────────────────────────────────────────────

object GameplayUtil {
    fun getMySpawn(): StructureSpawn = getObjectsByPrototype(StructureSpawn::class.js).toList()
        .first { it.my == true }

    fun getMySource(): Source {
        val sources = getObjectsByPrototype(Source::class.js).toList()
        return getMySpawn().findClosestByRange(sources.toTypedArray())
    }

    fun getMyCreeps(): List<Creep> = getObjectsByPrototype(Creep::class.js).toList()
        .filter { it.exists && it.my == true && it.hitsMax != 5000 }

    fun getMyEscortCreep(): EscortCreep? = getObjectsByPrototype(EscortCreep::class.js).toList()
        .firstOrNull { it.my == true }
}

// ── Gameplay ──────────────────────────────────────────────────────────────────

class Gameplay {
    val mySpawn: StructureSpawn = getMySpawn()
    val mySource: Source = getMySource()
    val myCreeps: List<Creep> = getMyCreeps()
    val myEscortCreep: EscortCreep? = getMyEscortCreep()

    init {
        // setDirections spawn előtt dinamikusan
    }

    fun getEnemyCreeps(): List<Creep> = getObjectsByPrototype(Creep::class.js).toList()
        .filter { it.exists && it.my == false  }

    fun getNearestEnemy(from: Creep): Creep? =
        getEnemyCreeps().minByOrNull { from.getRangeTo(it) }

    fun getFocusFireTarget(): Creep? =
        getEnemyCreeps().minByOrNull { it.hits }

    fun getMostWounded(): Creep? =
        myCreeps.filter { it.hits < it.hitsMax }.minByOrNull { it.hits }

    // EscortCreep várakozó pozíciója
    fun getEscortWaitPosition(): Position =
        if (mySpawn.y < 50) pos(12, 12) else pos(12, 87)

    // Combat gyülekező pont
    fun getCombatRallyPoint(): Position =
        if (mySpawn.y < 50) pos(49, 49) else pos(49, 50)

    // Capture target: saját flag (kígyó vezető ide megy, EscortCreep is)
    fun getCaptureTarget(): Position? {
        val flags = getObjectsByPrototype(Flag::class.js).toList()
        return flags.firstOrNull { it.my == true }
            ?: getObjectsByPrototype(StructureSpawn::class.js).toList()
                .firstOrNull { it.my == false }
    }
}

private fun pos(x: Int, y: Int): Position = object : Position {
    override var x = x
    override var y = y
}

// ── Role assignálás ───────────────────────────────────────────────────────────

private fun assignStaticRoles(gameplay: Gameplay) {
    val unassigned = gameplay.myCreeps
        .filter { !it.hasRole() }
        .sortedBy { it.id }

    for (creep in unassigned) {
        val workerCount    = gameplay.myCreeps.count { it.role == Role.WORKER }
        val harvesterCount = gameplay.myCreeps.count { it.role == Role.HARVESTER }

        if (workerCount <= harvesterCount && workerCount < 2) {
            creep.role = Role.WORKER
        } else if (harvesterCount < workerCount && harvesterCount < 2) {
            creep.role = Role.HARVESTER
        } else if (creep.canAttack() || creep.canHeal()) {
            // HYBRID vagy RANGER → harci csapat
            creep.role = if (creep.canHeal()) Role.COMBAT_HYBRID else Role.COMBAT_RANGER
        } else {
            // Csak MOVE → kígyó tag
            creep.role = Role.SNAKE
        }
    }
}

// ── Spawn queue ───────────────────────────────────────────────────────────────
// Gazdasági alap: W, H, W, H
// Majd combat loop: HYBRID, RANGER x8 folyamatosan
// Majd kígyó: 30x MOVE_ONLY

private val economyQueue: MutableList<CreepType> = mutableListOf(
    CreepType.WORKER,
    CreepType.HARVESTER,
    CreepType.WORKER,
    CreepType.HARVESTER,
)

// Combat loop – folyamatosan ismétlődik
private val combatPattern = listOf(
    CreepType.HYBRID,
    CreepType.RANGER,
    CreepType.HYBRID,
    CreepType.RANGER,
    CreepType.HYBRID,
    CreepType.RANGER,
    CreepType.HYBRID,
    CreepType.RANGER,
)

// Kígyó queue
private val snakeQueue: MutableList<CreepType> = MutableList(30) { CreepType.MOVE_ONLY }

// Combat creep számláló – ha valaki meghal, újra kell spawnolni
private var totalCombatSpawned = 0
private val MAX_COMBAT = 8

private fun getNextCreepToSpawn(gameplay: Gameplay): CreepType? {
    // 1. Gazdasági creepek először
    if (economyQueue.isNotEmpty()) return economyQueue.first()

    // 2. Ha nincs meg a 8 harci creep → combat
    val aliveCombat = gameplay.myCreeps.count {
        it.role == Role.COMBAT_HYBRID || it.role == Role.COMBAT_RANGER
    }
    if (aliveCombat < MAX_COMBAT) {
        val index = totalCombatSpawned % combatPattern.size
        return combatPattern[index]
    }

    // 3. Kígyó
    if (snakeQueue.isNotEmpty()) return snakeQueue.first()

    return null
}

private fun onSpawnSuccess(type: CreepType) {
    when (type) {
        in economyQueue -> economyQueue.removeFirst()
        CreepType.HYBRID, CreepType.RANGER -> totalCombatSpawned++
        CreepType.MOVE_ONLY -> snakeQueue.removeFirst()
        else -> {}
    }
}

// ── Loop ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    val gameplay = Gameplay()
    val mySpawn = gameplay.mySpawn
    val isTopSide = mySpawn.y < 50

    // Spawn
    if (mySpawn.spawning == null) {
        val next = getNextCreepToSpawn(gameplay)
        if (next != null) {
            val spawnDirection: Array<DirectionConstant> = when {
                isTopSide  && next == CreepType.WORKER -> arrayOf<DirectionConstant>(TOP_LEFT)
                !isTopSide && next == CreepType.WORKER -> arrayOf<DirectionConstant>(BOTTOM_LEFT)
                else -> arrayOf<DirectionConstant>(TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT)
            }
            mySpawn.setDirections(spawnDirection)
            val result = CreepFactory.of(next).createCreep(mySpawn)
            if (result != null) onSpawnSuccess(next)
        }
    }

    // Role assignálás
    assignStaticRoles(gameplay)

    // Formation frissítések
    CombatManager.update(gameplay)
    SnakeManager.update(gameplay)

    // Behavior assignálás
    BehaviorSelector.assignAll(gameplay)

    // EscortCreep vezérlése
    gameplay.myEscortCreep?.let { escort ->
        val snakeLeader = SnakeManager.getLeader()
        if (snakeLeader == null) {
            // Még nincs kígyó → várakozó pozícióra megy
            val waitPos = gameplay.getEscortWaitPosition()
            if (escort.getRangeTo(waitPos) > 1) escort.moveTo(waitPos)
        } else {
            // Van kígyó → követi a vezető mögött (2. helyen)
            val followTarget = SnakeManager.getEscortFollowTarget()
            if (followTarget != null && escort.getRangeTo(followTarget) > 2) {
                escort.moveTo(followTarget)
            }
            // Ha a vezető elérte a flaget → EscortCreep lép a flagre
            val flag = gameplay.getCaptureTarget()
            if (flag != null && snakeLeader.getRangeTo(flag) <= 1) {
                escort.moveTo(flag)
            }
        }
    }

    // Creepek végrehajtása
    for (creep in gameplay.myCreeps) {
        creep.execute(gameplay)
    }
}