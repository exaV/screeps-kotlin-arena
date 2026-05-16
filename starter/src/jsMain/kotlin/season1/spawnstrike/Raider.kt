package season1.spawnstrike

import screeps.api.*
import season1.spawnstrike.CombatTuning.RANGED_ATTACK_RANGE
import season1.spawnstrike.SpawnStrikeStrategy.RAIDER_COMBAT_ASSAULT_START_TICK

object Raider {

    private var myFlagReached = false
    private var entryPointReached = false

    // Tartó pozíció: bottom → (52,21), top → (47,78)
    private val BOTTOM_HOLD = pos(52, 21)
    private val TOP_HOLD    = pos(47, 78)

    // Várakozó/visszatérési pont középen: bottom → (44,44), top → (55,55)
    private val BOTTOM_ENTRY   = pos(44, 44)
    private val TOP_ENTRY      = pos(55, 55)

    // Kötelező waypoint az enemy spawn előtt: lent → (73,8), fent → (25,91)
    private val BOTTOM_WAYPOINT = pos(73, 8)
    private val TOP_WAYPOINT    = pos(25, 91)

    private var waypointReached = false

    private const val HOSTILE_RETREAT_RANGE = 10

    fun execute(creep: Creep, gameplay: Gameplay) {

        val myFlag    = gameplay.getMyCaptureFlag()
        val enemyFlags = gameplay.flagsToCapture()

        // --- RAIDER logika (flag capture) ---
        if (myFlag != null && creep.role == Role.RAIDER) {
            if (creep.findInRange(arrayOf(myFlag), 0).isNotEmpty()) {
                myFlagReached = true
            }

            if (enemyFlags.isEmpty()) {
                val holdPos  = if (gameplay.isInTop()) TOP_HOLD else BOTTOM_HOLD
                val hostiles = gameplay.getHostileCreeps()
                tryRanged(creep, hostiles)
                if (creep.getRangeTo(holdPos) > 0) creep.moveTo(holdPos)
                return
            }

            if (myFlagReached && enemyFlags.isNotEmpty()) {
                creep.moveTo(enemyFlags.first())
            }
            creep.moveTo(myFlag)
            return
        }

        // --- RAIDER_COMBAT logika ---
        if (creep.role == Role.RAIDER_COMBAT) {
            val hostiles   = gameplay.getHostileCreeps()
            val entryPoint = if (gameplay.isInTop()) TOP_ENTRY else BOTTOM_ENTRY
            val enemySpawn = gameplay.getEnemySpawn()

            // Enemy spawn 15 range-en belül → minden szabály felfüggesztve, csak támad
            if (enemySpawn != null && creep.getRangeTo(enemySpawn) <= 15) {
                val distToSpawn = creep.getRangeTo(enemySpawn)
                // Creepeket lő ha van, különben a spawnt
                val shotCreep = tryRanged(creep, hostiles)
                if (!shotCreep && distToSpawn <= RANGED_ATTACK_RANGE && creep.canRangedAttack()) {
                    creep.rangedAttack(enemySpawn)
                }
                if (distToSpawn > RANGED_ATTACK_RANGE) creep.moveTo(enemySpawn)
                return
            }

            // Ha valaki 10 range-en belül van → visszatér a középpontra és ott vár, de lő
            val tooClose = hostiles.any { creep.getRangeTo(it) <= HOSTILE_RETREAT_RANGE }
            if (tooClose) {
                tryRanged(creep, hostiles)
                if (creep.getRangeTo(entryPoint) > 1) creep.moveTo(entryPoint)
                return
            }

            // Senki nincs közel → normál haladás
            if (!entryPointReached) {
                if (creep.x == entryPoint.x && creep.y == entryPoint.y) {
                    entryPointReached = true
                } else {
                    creep.moveTo(entryPoint)
                    return
                }
            }

            // Entry point elérve → waypoint felé, majd enemy spawn felé
            if (enemyFlags.isNotEmpty()) {
                creep.moveTo(enemyFlags.first())
                return
            }

            if (getTicks() > RAIDER_COMBAT_ASSAULT_START_TICK) {
                val waypoint = if (gameplay.isInTop()) TOP_WAYPOINT else BOTTOM_WAYPOINT
                if (!waypointReached) {
                    if (creep.getRangeTo(waypoint) <= 1) {
                        waypointReached = true
                    } else {
                        creep.moveTo(waypoint)
                        return
                    }
                }
                val spawn = gameplay.getEnemySpawn() ?: return
                if (creep.rangedAttack(spawn) != OK) {
                    creep.moveTo(spawn)
                }
            }
        }
    }

    private fun tryRanged(creep: Creep, hostiles: List<Creep>): Boolean {
        val inRange = hostiles.filter { creep.getRangeTo(it) <= CombatTuning.RANGED_ATTACK_RANGE }
        if (inRange.isEmpty()) return false
        if (inRange.size >= 2) creep.rangedMassAttack()
        else creep.rangedAttack(inRange.first())
        return true
    }
}

fun creepInCenter(creep: Creep, centerX: Int = 50, centerY: Int = 50, radius: Int = 8): Boolean {
    val dx = creep.x - centerX
    val dy = creep.y - centerY
    return dx * dx + dy * dy <= radius * radius
}