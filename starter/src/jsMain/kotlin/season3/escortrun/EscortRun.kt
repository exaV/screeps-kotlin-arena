package season3.escortrun

import screeps.api.*
import screeps.api.season3.EscortCreep
import screeps.api.structures.*
import season3.escortrun.combat.CombatTuning
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

    fun getMySpawns(): List<StructureSpawn> =
        getObjectsByPrototype(StructureSpawn::class.js).toList()
            .filter { it.exists && it.my == true }

    /** Távoli bővítő konténer körüli hub (alsó spawn: Y tükrözve). */
    fun expansionHubPosition(): Position = pos(
        EscortRunStrategy.EXPANSION_CONTAINER_X,
        if (mySpawn.y < 50) EscortRunStrategy.EXPANSION_CONTAINER_Y_TOP
        else 99 - EscortRunStrategy.EXPANSION_CONTAINER_Y_TOP,
    )

    init {
        // setDirections spawn előtt dinamikusan
    }

    fun getEnemyCreeps(): List<Creep> = getObjectsByPrototype(Creep::class.js).toList()
        .filter { it.exists && it.my == false && it.hitsMax != 5000 }

    /** Minden ellenséges creep (VIP is) – célválasztás / lövés. */
    fun getHostileCreeps(): List<Creep> = getObjectsByPrototype(Creep::class.js).toList()
        .filter { it.exists && it.my == false }

    fun getHostileConstructionSites(): List<ConstructionSite> =
        getObjectsByPrototype(ConstructionSite::class.js).toList()
            .filter { it.exists && it.my != true }

    fun getEnemySpawns(): List<StructureSpawn> =
        getObjectsByPrototype(StructureSpawn::class.js).toList()
            .filter { it.exists && it.my == false }

    // ── Target prioritás (EscortRunStrategy + eredeti komment a forrásban) ───
    // 1. Ha az ellenfél VIP a saját zászlónk 25 range-en belül → ő a cél (capture deny)
    // 2. Saját VIP melletti blokkolók
    // 3. Ellenséges bővítés (építőhely / második spawn) – ne versenyezzen le 2 spawn ellen passzívan
    // 4. Ellen VIP körüli healer → támadó → maga a VIP
    // 5. Nincs ellen VIP → legközelebbi nem-VIP

    fun getPriorityTarget(from: Creep): Creep? {
        val allEnemies = getObjectsByPrototype(Creep::class.js).toList()
            .filter { it.exists && it.my == false }
        if (allEnemies.isEmpty()) return null

        val enemyEscort = allEnemies.firstOrNull { it.hitsMax == 5000 }
        val enemyNonEscort = allEnemies.filter { it.hitsMax != 5000 }
        val myFlag = getCaptureTarget()

        if (enemyEscort != null && myFlag != null &&
            enemyEscort.getRangeTo(myFlag) <= EscortRunStrategy.ENEMY_VIP_FLAG_DENY_RANGE
        ) {
            return enemyEscort
        }

        val myVip = myEscortCreep
        if (myVip != null) {
            val blocker = enemyNonEscort
                .filter { it.getRangeTo(myVip) <= 5 }
                .minWithOrNull(compareBy({ it.hits }, { from.getRangeTo(it) }))
            if (blocker != null && from.getRangeTo(blocker) <= 22) return blocker
        }

        val hostileSites = getHostileConstructionSites()
        if (hostileSites.isNotEmpty()) {
            val br = CombatTuning.ENEMY_NEAR_HOSTILE_BUILD_RANGE
            val buildCreep = enemyNonEscort
                .filter { ec -> hostileSites.any { site -> ec.getRangeTo(site) <= br } }
                .minWithOrNull(compareBy({ from.getRangeTo(it) }, { it.hits }))
            if (buildCreep != null) return buildCreep
        }
        val enemySpawns = getEnemySpawns()
        if (enemySpawns.size >= 2) {
            val farthestSpawn = enemySpawns.maxByOrNull { it.getRangeTo(mySpawn) }
            if (farthestSpawn != null) {
                val sr = CombatTuning.ENEMY_NEAR_EXTRA_SPAWN_RANGE
                val nearExtra = enemyNonEscort
                    .filter { it.getRangeTo(farthestSpawn) <= sr }
                    .minByOrNull { from.getRangeTo(it) }
                if (nearExtra != null) return nearExtra
            }
        }

        if (enemySpawns.isNotEmpty()) {
            val theirSpawnNearUs = enemySpawns.minByOrNull { it.getRangeTo(mySpawn) }!!
            val expander = enemyNonEscort.filter {
                it.getRangeTo(mySpawn) >= CombatTuning.ENEMY_DEEP_ECON_RAIDER_RANGE &&
                    it.body.any { p -> p.type == WORK || p.type == CARRY } &&
                    it.getRangeTo(theirSpawnNearUs) >= 22
            }.minByOrNull { from.getRangeTo(it) }
            if (expander != null) return expander
        }

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

    /** Régi középponti rally – BehaviorSelector deathball-detektorhoz. */
    fun getMapCenterRally(): Position =
        if (mySpawn.y < 50) pos(49, 49) else pos(49, 50)

    /**
     * Alapból **staging** a spawn és a saját zászló között (saját fél) – a hybrid nem a (49,49)
     * körüli ellenséghalomba vonul. Deny fázisban közelebb a zászlóhoz csúszunk.
     * Utána: ha a harcosok szétnyíltak és nincs közeli ellenség, a rally a **hybrid** (vagy centroid) felé
     * csúszik, hogy a heal távolságon belül maradjanak.
     */
    fun getCombatRallyPoint(): Position {
        val goal = getCaptureTarget()
        val sx = mySpawn.x
        val sy = mySpawn.y
        val enemyVip = getObjectsByPrototype(Creep::class.js).toList()
            .firstOrNull { it.exists && it.my == false && it.hitsMax == 5000 }
        val denyPhase = goal != null && enemyVip != null &&
            enemyVip.getRangeTo(goal) <= EscortRunStrategy.ENEMY_VIP_FLAG_DENY_RANGE

        val baseRally = when {
            denyPhase && goal != null -> {
                val t = 0.68
                pos(
                    (sx + (goal.x - sx) * t).toInt().coerceIn(2, 97),
                    (sy + (goal.y - sy) * t).toInt().coerceIn(2, 97),
                )
            }
            getHostileConstructionSites().isNotEmpty() -> {
                val threat = getHostileConstructionSites().minByOrNull { mySpawn.getRangeTo(it) }!!
                val t = CombatTuning.RALLY_TOWARD_THREAT_T
                pos(
                    (sx + (threat.x - sx) * t).toInt().coerceIn(2, 97),
                    (sy + (threat.y - sy) * t).toInt().coerceIn(2, 97),
                )
            }
            getEnemySpawns().size >= 2 -> {
                val threatSpawn = getEnemySpawns().maxByOrNull { mySpawn.getRangeTo(it) }!!
                val t = CombatTuning.RALLY_TOWARD_THREAT_T
                pos(
                    (sx + (threatSpawn.x - sx) * t).toInt().coerceIn(2, 97),
                    (sy + (threatSpawn.y - sy) * t).toInt().coerceIn(2, 97),
                )
            }
            goal != null -> {
                val t = EscortRunStrategy.DEFAULT_RALLY_SPAWN_TO_FLAG_T
                pos(
                    (sx + (goal.x - sx) * t).toInt().coerceIn(2, 97),
                    (sy + (goal.y - sy) * t).toInt().coerceIn(2, 97),
                )
            }
            else -> getMapCenterRally()
        }
        return cohesionBlendRally(baseRally)
    }

    private fun combatFighters(): List<Creep> =
        myCreeps.filter { it.role == Role.COMBAT_HYBRID || it.role == Role.COMBAT_RANGER }

    private fun combatPackMaxSpread(): Int {
        val f = combatFighters()
        if (f.size < 2) return 0
        return f.maxOf { a -> f.maxOf { b -> a.getRangeTo(b) } }
    }

    private fun hostilesNearCombatPack(): Boolean =
        getHostileCreeps().any { h ->
            combatFighters().any { it.getRangeTo(h) <= CombatTuning.COHESION_SUSPEND_HOSTILE_RANGE }
        }

    /** Stratégiai rally + összetartás: hybrid felé húzás, ha szétestek és nincs közeli harc. */
    private fun cohesionBlendRally(base: Position): Position {
        val fighters = combatFighters()
        if (fighters.size < 2) return base
        if (hostilesNearCombatPack()) return base
        if (combatPackMaxSpread() <= CombatTuning.COMBAT_COHESION_MAX_SPREAD) return base

        val hybrid = fighters.firstOrNull { it.role == Role.COMBAT_HYBRID }
        val (cx, cy) = if (hybrid != null) {
            hybrid.x to hybrid.y
        } else {
            val n = fighters.size
            fighters.sumOf { it.x } / n to fighters.sumOf { it.y } / n
        }
        val w = CombatTuning.COHESION_RALLY_BLEND_WEIGHT
        val rx = (base.x * (1 - w) + cx * w).toInt().coerceIn(2, 97)
        val ry = (base.y * (1 - w) + cy * w).toInt().coerceIn(2, 97)
        return pos(rx, ry)
    }

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
        if (creep.isExpansionRunnerBody()) {
            creep.role = Role.EXPANSION_BUILDER
            continue
        }
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

// ── Spawn queue (részletek: EscortRunStrategy.kt) ─────────────────────────────
// W, H, majd 3. slot: SKIRMISHER (ellenség >20-re kiment) vagy EXPANSION_RUNNER (passzív ellenfél)

private val economyQueue: MutableList<CreepType> = mutableListOf(
    CreepType.WORKER,
    CreepType.HARVESTER,
)
private var economyOpenerResolved: Boolean = false

// Minden 3. harci creep HYBRID (heal + ranged), különben gyors RANGER/SKIRMISHER sorozat
private val combatPattern = listOf(
    CreepType.RANGER,
    CreepType.SKIRMISHER,
    CreepType.HYBRID,
)

private val snakeQueue: MutableList<CreepType> =
    MutableList(EscortRunStrategy.SNAKE_TOTAL) { CreepType.MOVE_ONLY }
private var totalCombatSpawned = 0
private var snakeDone = false
private var fullEconomyBuilt = false
/** Sikeresen lespawnoltunk legalább egy HYBRID-et (harci sorozatból). */
private var hasSpawnedFirstHybrid = false

/** Push (ellenséges VIP elindult) alatt már lefutott a kötelező első harci HYBRID slot. */
private var interceptCombatLeadHybridDone = false

private fun enemyEscortNearSpawn(gameplay: Gameplay): Boolean {
    val enemyEscort = getObjectsByPrototype(Creep::class.js).toList()
        .firstOrNull { it.exists && it.my == false && it.hitsMax == 5000 }
    val enemySpawn = getObjectsByPrototype(screeps.api.structures.StructureSpawn::class.js).toList()
        .firstOrNull { it.my == false }
    if (enemyEscort == null || enemySpawn == null) return true
    return enemyEscort.getRangeTo(enemySpawn) <= EscortRunStrategy.ENEMY_VIP_NEAR_OWN_SPAWN_RANGE
}

/**
 * Az ellenséges VIP már elhagyta a saját spawnját ([enemyEscortNearSpawn] hamis) és még él –
 * nincs idő nehéz RANGER + második HYBRID ütemre: könnyű skirmisherek kellenek.
 */
private fun enemyEscortPushActive(gameplay: Gameplay): Boolean {
    if (getObjectsByPrototype(Creep::class.js).toList()
            .none { it.exists && it.my == false && it.hitsMax == 5000 }
    ) {
        return false
    }
    return !enemyEscortNearSpawn(gameplay)
}

/**
 * Rush ellen: a minta RANGER → SKIRMISHER; HYBRID slot → SKIRMISHER, ha már volt hybrid a pushban
 * (az első harci HYBRID slotot külön kezeli a pickCombatSpawnType).
 */
private fun resolveCombatSpawnType(gameplay: Gameplay, patternType: CreepType): CreepType {
    if (!enemyEscortPushActive(gameplay)) return patternType
    return when (patternType) {
        CreepType.RANGER -> CreepType.SKIRMISHER
        CreepType.HYBRID -> if (!hasSpawnedFirstHybrid) CreepType.HYBRID else CreepType.SKIRMISHER
        else -> patternType
    }
}

/** Harci creep típus: push alatt először HYBRID, utána a minta + [resolveCombatSpawnType]. */
private fun pickCombatSpawnType(gameplay: Gameplay): CreepType {
    if (enemyEscortPushActive(gameplay) && !interceptCombatLeadHybridDone) {
        val haveCombatHybrid = gameplay.myCreeps.any { it.exists && it.role == Role.COMBAT_HYBRID }
        if (!haveCombatHybrid) {
            return CreepType.HYBRID
        }
        interceptCombatLeadHybridDone = true
    }
    val index = totalCombatSpawned % combatPattern.size
    return resolveCombatSpawnType(gameplay, combatPattern[index])
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

private fun anyEnemyBeyondDistanceFromTheirMainSpawn(gameplay: Gameplay, beyond: Int): Boolean {
    val hostileSpawns = gameplay.getEnemySpawns()
    if (hostileSpawns.isEmpty()) return false
    val theirMain = hostileSpawns.minByOrNull { it.getRangeTo(gameplay.mySpawn) } ?: return false
    return gameplay.getHostileCreeps().any { creep ->
        creep.hitsMax != 5000 && creep.getRangeTo(theirMain) > beyond
    }
}

private fun economyOpenerCreepType(gameplay: Gameplay): CreepType =
    if (anyEnemyBeyondDistanceFromTheirMainSpawn(
            gameplay,
            EscortRunStrategy.ENEMY_LEFT_SPAWN_FOR_SKIRMISHER,
        )
    ) {
        CreepType.SKIRMISHER
    } else {
        CreepType.EXPANSION_RUNNER
    }

private fun getNextCreepToSpawn(gameplay: Gameplay): CreepType? {
    if (!enemyEscortPushActive(gameplay)) {
        interceptCombatLeadHybridDone = false
    }

    val revival = needsRevival(gameplay)
    if (revival != null && economyQueue.isEmpty()) {
        if (economyOpenerResolved) return revival
        val workers = gameplay.myCreeps.count { it.role == Role.WORKER }
        val harvesters = gameplay.myCreeps.count { it.role == Role.HARVESTER }
        if (workers < 1 || harvesters < 1) return revival
    }

    if (economyQueue.isNotEmpty()) {
        return economyQueue.first()
    }

    if (!economyOpenerResolved) {
        return economyOpenerCreepType(gameplay)
    }

    if (!fullEconomyBuilt && enemyEscortNearSpawn(gameplay)) {
        fullEconomyBuilt = true
        economyQueue.addAll(listOf(CreepType.WORKER, CreepType.HARVESTER))
        return economyQueue.first()
    }

    val aliveCombat = gameplay.myCreeps.count {
        it.role == Role.COMBAT_HYBRID || it.role == Role.COMBAT_RANGER
    }
    if (aliveCombat < EscortRunStrategy.MAX_COMBAT_ALIVE) {
        return pickCombatSpawnType(gameplay)
    }

    if (snakeQueue.isNotEmpty()) return snakeQueue.first()

    snakeDone = true
    return pickCombatSpawnType(gameplay)
}

private fun onSpawnSuccess(type: CreepType, gameplay: Gameplay) {
    when {
        economyQueue.firstOrNull() == type -> economyQueue.removeFirst()
        !economyOpenerResolved && (type == CreepType.SKIRMISHER || type == CreepType.EXPANSION_RUNNER) -> {
            economyOpenerResolved = true
            if (type == CreepType.SKIRMISHER) totalCombatSpawned++
        }
        type == CreepType.HYBRID || type == CreepType.RANGER || type == CreepType.SKIRMISHER -> {
            totalCombatSpawned++
            if (type == CreepType.HYBRID) {
                hasSpawnedFirstHybrid = true
                if (enemyEscortPushActive(gameplay)) {
                    interceptCombatLeadHybridDone = true
                }
            }
        }
        type == CreepType.MOVE_ONLY -> snakeQueue.removeFirst()
    }
}

// ── Loop ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    val gameplay = Gameplay()
    val idleSpawns = gameplay.getMySpawns().filter { it.spawning == null }
    if (idleSpawns.isNotEmpty()) {
        val next = getNextCreepToSpawn(gameplay)
        if (next != null) {
            for (spawn in idleSpawns.sortedBy { it.id }) {
                val isTopSide = spawn.y < 50
                val spawnDirection: Array<DirectionConstant> = when {
                    isTopSide && next == CreepType.WORKER ->
                        arrayOf<DirectionConstant>(TOP_LEFT)
                    !isTopSide && next == CreepType.WORKER ->
                        arrayOf<DirectionConstant>(BOTTOM_LEFT)
                    else -> arrayOf<DirectionConstant>(
                        TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT,
                    )
                }
                spawn.setDirections(spawnDirection)
                val result = CreepFactory.of(next).createCreep(spawn)
                if (result != null) {
                    onSpawnSuccess(next, gameplay)
                    break
                }
            }
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
            if (!enemyEscortNearSpawn(gameplay) && flag != null) {
                escort.moveTo(flag)
            } else {
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