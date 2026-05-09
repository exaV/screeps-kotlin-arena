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

    // EscortCreep kizárása hitsMax alapján (5000 HP = EscortCreep)
    // Azért nem `it is EscortCreep`, mert JS external class instanceof nem megbízható
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
        // setDirections spawn előtt, creep típusonként dinamikusan
    }

    fun getEnemyCreeps(): List<Creep> = getObjectsByPrototype(Creep::class.js).toList()
        .filter { it.exists && it.my == false && it.hitsMax != 5000 }

    fun getNearestEnemy(from: Creep): Creep? =
        getEnemyCreeps().minByOrNull { from.getRangeTo(it) }

    fun getFocusFireTarget(): Creep? =
        getEnemyCreeps().minByOrNull { it.hits }

    fun getMostWounded(): Creep? =
        myCreeps.filter { it.hits < it.hitsMax }.minByOrNull { it.hits }

    fun getCaptureTarget(): Position? {
        val flags = getObjectsByPrototype(Flag::class.js).toList()
        val myFlag = flags.firstOrNull { it.my == true }
        if (myFlag != null) return myFlag
        return getObjectsByPrototype(StructureSpawn::class.js).toList()
            .firstOrNull { it.my == false }
    }
}

// ── Role assignálás ───────────────────────────────────────────────────────────
// Spawn sorrend alapján (id növekvő = korábbi spawn):
//   1. creep → WORKER, 2. → HARVESTER, 3. → WORKER, 4. → HARVESTER, 5.+ → combat

private fun assignStaticRoles(gameplay: Gameplay) {
    val unassigned = gameplay.myCreeps
        .filter { !it.hasRole() }
        .sortedBy { it.id }

    for (creep in unassigned) {
        // Minden iterációban újraszámoljuk, mert az előző iteráció már assignált
        val workerCount    = gameplay.myCreeps.count { it.role == Role.WORKER }
        val harvesterCount = gameplay.myCreeps.count { it.role == Role.HARVESTER }

        if (workerCount <= harvesterCount && workerCount < 2) {
            creep.role = Role.WORKER
        } else if (harvesterCount < workerCount && harvesterCount < 2) {
            creep.role = Role.HARVESTER
        }
        // else: combat creep → FormationManager.initialize() kezeli
    }
}

// ── Spawn queue – modul szintű, tickek között megmarad ───────────────────────

private val requiredCreeps: MutableList<CreepType> = mutableListOf(
    CreepType.WORKER,
    CreepType.HARVESTER,
    CreepType.WORKER,
    CreepType.HARVESTER,
    CreepType.ATTACKER,
    CreepType.HYBRID,
    CreepType.HYBRID,
)

// ── Loop ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    val gameplay = Gameplay()
    val mySpawn = gameplay.mySpawn

    // Spawn következő creep – csak ha sikeres, vesszük ki a listából
    if (mySpawn.spawning == null && requiredCreeps.isNotEmpty()) {
        val next = requiredCreeps.first()
        // Spawn iránya creep típusonként:
        // W2 és H1 → TOP_LEFT (8,8-ra spawol a 9,9-ről)
        // többi → minden irány
        val isTopSide = mySpawn.y < 50
        val spawnDirection: Array<DirectionConstant> = when {
            isTopSide && next == CreepType.WORKER     -> arrayOf<DirectionConstant>(TOP_LEFT)
            !isTopSide && next == CreepType.WORKER    -> arrayOf<DirectionConstant>(BOTTOM_LEFT)
            else -> arrayOf<DirectionConstant>(TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT)
        }
        mySpawn.setDirections(spawnDirection)
        val result = CreepFactory.of(next).createCreep(mySpawn)
        if (result != null) {
            requiredCreeps.removeFirst()
        }
    }

    // 1. Statikus role-ok: Worker, Harvester
    assignStaticRoles(gameplay)

    // 2. Combat role-ok: Leader, Follower
    val combatCreeps = gameplay.myCreeps.filter {
        !it.hasRole() || it.role == Role.LEADER || it.role == Role.FOLLOWER
    }
    FormationManager.initialize(combatCreeps)
    FormationManager.promoteIfNeeded()

    // 3. Behavior assignálás
    BehaviorSelector.assignAll(gameplay)

    // 4. EscortCreep vezérlése: menjen a saját flaghoz
    gameplay.myEscortCreep?.let { escort ->
        val target = gameplay.getCaptureTarget()
        if (target != null) escort.moveTo(target)
    }

    // 5. Execute
    for (creep in gameplay.myCreeps) {
        creep.execute(gameplay)
    }
}