package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*

class Gameplay(
    var mySpawn: StructureSpawn = GameplayUtil.getMySpawn(),
    var myCreeps: List<Creep> = GameplayUtil.getMyCreeps(),
) {
    // Opening queue: RAIDER, RAIDER_COMBAT x2, ASSAULT x2, SCOUT
    private val openingSpawnQueue: MutableList<Role> = mutableListOf(
        Role.RAIDER,
        Role.RAIDER_COMBAT,
        Role.RAIDER_COMBAT,
        Role.ASSAULT,
        Role.ASSAULT,
        Role.SCOUT,
    )

    fun analyze() {
        mySpawn = GameplayUtil.getMySpawn()
        myCreeps = GameplayUtil.getMyCreeps()
    }

    fun spawnCreep() {
        mySpawn.takeIf { it.spawning == null }?.let { spawn ->
            val role = nextSpawnRole() ?: return@let
            val body = bodyForRole(role)
            val creep = spawn.spawnCreep(body.toTypedArray()).`object` ?: return@let
            creep.role = role
            if (openingSpawnQueue.isNotEmpty()) openingSpawnQueue.removeFirst()
        }
    }

    private fun bodyForRole(role: Role): List<BodyPartType> = when (role) {
        Role.RAIDER        -> listOf(MOVE)
        Role.RAIDER_COMBAT -> listOf(MOVE, RANGED_ATTACK)
        Role.ASSAULT       -> List(16) { MOVE } + List(20) { RANGED_ATTACK } + listOf(HEAL)
        Role.SCOUT         -> listOf(TOUGH, MOVE)  // TOUGH megkülönbözteti a RAIDER-től
    }

    private fun nextSpawnRole(): Role? {
        return openingSpawnQueue.firstOrNull()
    }

    fun assignStaticRoles() {
        val unassigned = myCreeps.filter { !it.hasRole() }.sortedBy { it.id }
        for (creep in unassigned) {
            creep.role = when {
                creep.canRangedAttack() && creep.canHeal()        -> Role.ASSAULT
                creep.canRangedAttack()                            -> Role.RAIDER_COMBAT
                creep.body.any { it.type == TOUGH }               -> Role.SCOUT
                else                                               -> Role.RAIDER
            }
        }
    }

    // --- Defend mód ---

    /**
     * Defend mód ha bármely ellenség ≤10 range-re van:
     * - a saját spawntól, VAGY
     * - a bottom/top checkpointok bármelyikétől
     */
    fun isDefendMode(): Boolean {
        val hostiles = getHostileCreeps()
        if (hostiles.isEmpty()) return false

        val checkpoints = defendCheckpoints()
        return hostiles.any { h ->
            checkpoints.any { cp -> h.getRangeTo(cp) <= SpawnStrikeStrategy.DEFEND_CHECKPOINT_RANGE }
        }
    }

    private fun defendCheckpoints(): List<Position> {
        return if (isInTop()) listOf(
            mySpawnPos(),
            pos(85, 18),
            pos(91, 35),
            pos(78, 66),
        ) else listOf(
            mySpawnPos(),
            pos(13, 81),
            pos(8, 61),
            pos(21, 33),
        )
    }

    private fun mySpawnPos(): Position = pos(mySpawn.x, mySpawn.y)

    // --- Flanker/Assault wall-breach state ---
    fun isFlankerWallBreached(): Boolean = Assault.wallBreached

    // --- Rally pontok ---

    /** Gyülekezési/védelmi pont */
    fun getDefensiveRallyPoint(): Position =
        if (isInTop()) pos(51, 78) else pos(48, 21)

    fun getHostileCreeps(): List<Creep> =
        getObjectsByPrototype(Creep::class.js).toList().filter { it.exists && it.my == false }

    fun getEnemySpawns(): List<StructureSpawn> =
        getObjectsByPrototype(StructureSpawn::class.js).toList().filter { it.exists && it.my == false }

    fun getMyCaptureFlag(): Flag? =
        getObjectsByPrototype(Flag::class.js).toList().firstOrNull { it.exists && it.my == true }

    fun flagsToCapture(): List<Flag> =
        getObjectsByPrototype(Flag::class.js).toList().filter { it.exists && it.my != true }

    fun getEnemySpawn(): StructureSpawn? = getEnemySpawns().minByOrNull { mySpawn.getRangeTo(it) }

    fun isInTop(): Boolean = mySpawn.y < 50

    fun shouldAssaultEnemySpawn(): Boolean =
        Assault.wallBreached || getTicks() >= SpawnStrikeStrategy.ASSAULT_START_TICK
}

object GameplayUtil {
    fun getMySpawn(): StructureSpawn =
        getObjectsByPrototype(StructureSpawn::class.js).toList().first { it.my == true }

    fun getMyCreeps(): List<Creep> =
        getObjectsByPrototype(Creep::class.js).toList().filter { it.exists && it.my && !it.spawning }
}

fun pos(x: Int, y: Int): Position = object : Position {
    override var x = x
    override var y = y
}