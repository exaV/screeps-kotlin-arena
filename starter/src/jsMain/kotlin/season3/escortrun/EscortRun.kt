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
        .filter { it.exists && it.my == false && it.hitsMax != 5000 }

    fun getNearestEnemy(from: Creep): Creep? =
        getEnemyCreeps().minByOrNull { from.getRangeTo(it) }

    fun getFocusFireTarget(): Creep? =
        getEnemyCreeps().minByOrNull { it.hits }

    fun getMostWounded(): Creep? =
        myCreeps.filter { it.hits < it.hitsMax }.minByOrNull { it.hits }

    // ── Target prioritás ──────────────────────────────────────────────────────
    // 1. Ha az ellenfél EscortCreep 25 range-en belül van a saját flaghoz → ő a cél
    // 2. Különben: aki legközelebb van az ellenfél EscortCreephez
    // 3. Ha nincs EscortCreep → legkevesebb HP

    fun getPriorityTarget(from: Creep): Creep? {
        val allEnemies = getObjectsByPrototype(Creep::class.js).toList()
            .filter { it.exists && it.my == false }
        if (allEnemies.isEmpty()) return null

        val enemyEscort = allEnemies.firstOrNull { it.hitsMax == 5000 }
        val enemyNonEscort = allEnemies.filter { it.hitsMax != 5000 }

        // Ha van escort → prioritás: healer > támadó > escort
        if (enemyEscort != null) {
            // 1. Healer az escort közelében
            val healer = enemyNonEscort
                .filter { it.getRangeTo(enemyEscort) <= 8 &&
                        it.body.any { p -> p.type == screeps.api.HEAL } }
                .minByOrNull { from.getRangeTo(it) }
            if (healer != null) return healer

            // 2. Támadó az escort közelében
            val attacker = enemyNonEscort
                .filter { it.getRangeTo(enemyEscort) <= 8 &&
                        it.body.any { p -> p.type == screeps.api.RANGED_ATTACK || p.type == screeps.api.ATTACK } }
                .minByOrNull { from.getRangeTo(it) }
            if (attacker != null) return attacker

            // 3. Maga az escort
            return enemyEscort
        }

        // Nincs escort → legközelebb lévő ellenség
        return enemyNonEscort.minByOrNull { from.getRangeTo(it) }
    }
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
        // Ha tud healelni vagy ranged attackolni → harci creep, azonnal középre
        // Ez vonatkozik a korai HYBRID-re is, nem keveredik össze a Worker logikával
        if (creep.canHeal() || creep.canRangedAttack()) {
            creep.role = if (creep.canHeal()) Role.COMBAT_HYBRID else Role.COMBAT_RANGER
            continue
        }

        // Csak MOVE → kígyó
        if (!creep.canAttack() && !creep.canHeal() && !creep.canRangedAttack()) {
            // Ha van WORK part → Worker, ha csak MOVE/CARRY → Harvester vagy Snake
            val hasWork = creep.body.any { it.type == screeps.api.WORK }
            val hasCarry = creep.body.any { it.type == screeps.api.CARRY }

            val workerCount    = gameplay.myCreeps.count { it.role == Role.WORKER }
            val harvesterCount = gameplay.myCreeps.count { it.role == Role.HARVESTER }

            creep.role = when {
                hasWork                                                    -> Role.WORKER
                hasCarry && harvesterCount < 2                            -> Role.HARVESTER
                else                                                       -> Role.SNAKE
            }
        }
    }
}

// ── Spawn queue ───────────────────────────────────────────────────────────────
// Sorrend:
//   W1, H1, HYBRID(védelem), W2, H2
//   majd combat loop (8 db) folyamatosan ha valaki meghal
//   majd kígyó (30 db)
//   majd folyamatos combat erősítés

private val economyQueue: MutableList<CreepType> = mutableListOf(
    CreepType.WORKER,
    CreepType.HARVESTER,
    CreepType.HYBRID,    // korai védelem → rögtön középre megy
    // W2 és H2 dinamikusan kerül bele ha az ellenfél escort még a spawnnál van
)

private val combatPattern = listOf(
    CreepType.RANGER,
    CreepType.HYBRID,
)

private val snakeQueue: MutableList<CreepType> = MutableList(30) { CreepType.MOVE_ONLY }
private var totalCombatSpawned = 0
private val MAX_COMBAT = 8
private var snakeDone = false
private var fullEconomyBuilt = false  // W2+H2 már bekerült-e

// Az ellenfél escort spawnjától való távolság
private fun enemyEscortNearSpawn(gameplay: Gameplay): Boolean {
    val enemyEscort = getObjectsByPrototype(Creep::class.js).toList()
        .firstOrNull { it.exists && it.my == false && it.hitsMax == 5000 }
    val enemySpawn = getObjectsByPrototype(screeps.api.structures.StructureSpawn::class.js).toList()
        .firstOrNull { it.my == false }
    if (enemyEscort == null || enemySpawn == null) return true  // nem tudjuk → legyen biztonságos
    return enemyEscort.getRangeTo(enemySpawn) <= 5
}

private fun needsRevival(gameplay: Gameplay): CreepType? {
    val workers    = gameplay.myCreeps.count { it.role == Role.WORKER }
    val harvesters = gameplay.myCreeps.count { it.role == Role.HARVESTER }
    val targetWorkers    = if (fullEconomyBuilt) 2 else 1
    val targetHarvesters = if (fullEconomyBuilt) 2 else 1
    if (workers < targetWorkers)       return CreepType.WORKER
    if (harvesters < targetHarvesters) return CreepType.HARVESTER
    return null
}

private fun getNextCreepToSpawn(gameplay: Gameplay): CreepType? {
    // 0. Revival
    val revival = needsRevival(gameplay)
    if (revival != null && economyQueue.isEmpty()) return revival

    // 1. Gazdasági sor – ha W1+H1+HYBRID megvan és az escort még a spawnnál → W2+H2 bekerül
    if (economyQueue.isNotEmpty()) {
        val next = economyQueue.first()
        // Ha HYBRID már kiment és még nem döntöttük el W2+H2 kell-e
        if (next == economyQueue.last() && !fullEconomyBuilt) {
            // HYBRID az utolsó elem → utána döntünk
        }
        return next
    }

    // economyQueue kiürült – ha az escort még a spawnnál és W2+H2 még nem lett hozzáadva
    if (!fullEconomyBuilt && enemyEscortNearSpawn(gameplay)) {
        fullEconomyBuilt = true
        economyQueue.addAll(listOf(CreepType.WORKER, CreepType.HARVESTER))
        return economyQueue.first()
    }

    // 2. Combat: max 8
    val aliveCombat = gameplay.myCreeps.count {
        it.role == Role.COMBAT_HYBRID || it.role == Role.COMBAT_RANGER
    }
    if (aliveCombat < MAX_COMBAT) {
        val index = totalCombatSpawned % combatPattern.size
        return combatPattern[index]
    }

    // 3. Kígyó
    if (snakeQueue.isNotEmpty()) return snakeQueue.first()

    // 4. Folyamatos erősítés
    snakeDone = true
    val index = totalCombatSpawned % combatPattern.size
    return combatPattern[index]
}

private fun onSpawnSuccess(type: CreepType, gameplay: Gameplay) {
    when {
        economyQueue.firstOrNull() == type -> economyQueue.removeFirst()
        type == CreepType.HYBRID || type == CreepType.RANGER -> totalCombatSpawned++
        type == CreepType.MOVE_ONLY -> snakeQueue.removeFirst()
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
            if (result != null) onSpawnSuccess(next, gameplay)
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
        val flag = gameplay.getCaptureTarget()

        if (snakeLeader == null) {
            // Még nincs kígyó
            if (!enemyEscortNearSpawn(gameplay)) {
                // Ellenfél escort elindult → mi is induljunk, és lépjünk rá a flagra
                if (flag != null) escort.moveTo(flag)
            } else {
                // Ellenfél még a spawnnál → várakozó pozíció
                val waitPos = gameplay.getEscortWaitPosition()
                if (escort.getRangeTo(waitPos) > 1) escort.moveTo(waitPos)
            }
        } else if (flag != null && snakeLeader.getRangeTo(flag) <= 2) {
            // Leader megállt a flag közelében → EscortCreep rálép
            escort.moveTo(flag)
        } else {
            // Követi a leadert szorosan
            val followTarget = SnakeManager.getEscortFollowTarget()
            if (followTarget != null && escort.getRangeTo(followTarget) > 1) {
                escort.moveTo(followTarget)
            }
        }
    }

    // Creepek végrehajtása
    for (creep in gameplay.myCreeps) {
        creep.execute(gameplay)
    }
}