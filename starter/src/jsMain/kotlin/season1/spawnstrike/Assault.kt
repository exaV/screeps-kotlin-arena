package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*
import season1.spawnstrike.CombatTuning.RANGED_ATTACK_RANGE

object Assault {

    private const val SELF_HEAL_THRESHOLD = 0.95   // heal ha HP < 95% – minden tickben
    private const val MAX_COHESION_DIST   = 4       // ennél messzebb a társtól → várj rá

    // Bottom: falak sorrendben 6,44 -> 2,40
    private val BOTTOM_STAND       = pos(7, 44)
    private val BOTTOM_WALL_COORDS = listOf(pos(6, 44), pos(5, 43), pos(4, 42), pos(3, 41), pos(2, 40))

    // Top: falak sorrendben 93,55 -> 97,59
    private val TOP_STAND       = pos(92, 55)
    private val TOP_WALL_COORDS = listOf(pos(93, 55), pos(94, 56), pos(95, 57), pos(96, 58), pos(97, 59))

    var wallBreached = false
        private set

    fun execute(creep: Creep, gameplay: Gameplay) {
        // HEAL ÉS ATTACK UGYANABBAN A TICKBEN:
        // healSelf minden tickben lefut, utána a megfelelő behavior is lő – Screeps engedi
        healSelf(creep)

        if (gameplay.isDefendMode()) {
            defendBehavior(creep, gameplay)
            return
        }

        if (wallBreached) {
            pushBehavior(creep, gameplay)
            return
        }

        wallBreachBehavior(creep, gameplay)
    }

    // Heal minden tickben – NEM zárja ki a rangedAttack-ot ugyanabban a tickben
    private fun healSelf(creep: Creep) {
        if (!creep.canHeal()) return
        if (creep.hits.toDouble() / creep.hitsMax < SELF_HEAL_THRESHOLD) {
            creep.heal(creep)
        }
    }

    // Védekezés: lő + rally pont felé megy
    private fun defendBehavior(creep: Creep, gameplay: Gameplay) {
        shootBestTarget(creep, gameplay.getHostileCreeps())
        val rally = gameplay.getDefensiveRallyPoint()
        if (creep.getRangeTo(rally) > 2) creep.moveTo(rally)
    }

    // Fal fúrás: mindkét assault ugyanarra a falra lő, előre tolódik ahogy ledőlnek
    private fun wallBreachBehavior(creep: Creep, gameplay: Gameplay) {
        val isTop      = gameplay.isInTop()
        val wallCoords = if (isTop) TOP_WALL_COORDS else BOTTOM_WALL_COORDS
        val allWalls   = getObjectsByPrototype(StructureWall::class.js).toList().filter { it.exists }

        val nextWallCoord = wallCoords.firstOrNull { coord ->
            allWalls.any { w -> w.x == coord.x && w.y == coord.y }
        }

        if (nextWallCoord == null) {
            wallBreached = true
            return
        }

        val wallTarget = allWalls.first { w -> w.x == nextWallCoord.x && w.y == nextWallCoord.y }

        val prevCoordIdx = wallCoords.indexOf(nextWallCoord) - 1
        val standPos = if (prevCoordIdx < 0) {
            if (isTop) TOP_STAND else BOTTOM_STAND
        } else {
            wallCoords[prevCoordIdx]
        }

        // Ha van ellenség range-ben, azt lövi – álláspontot tartja
        val hostileInRange = gameplay.getHostileCreeps()
            .filter { creep.getRangeTo(it) <= RANGED_ATTACK_RANGE }
            .minByOrNull { it.hits }

        if (hostileInRange != null && creep.canRangedAttack()) {
            val cluster = gameplay.getHostileCreeps().count { creep.getRangeTo(it) <= RANGED_ATTACK_RANGE }
            if (cluster >= 2) creep.rangedMassAttack() else creep.rangedAttack(hostileInRange)
            if (creep.getRangeTo(standPos) > 1) creep.moveTo(standPos)
            return
        }

        // Falat lövi és előre tolódik a ledöntött falak helyére
        if (creep.getRangeTo(wallTarget) <= RANGED_ATTACK_RANGE) {
            creep.rangedAttack(wallTarget)
            if (creep.getRangeTo(standPos) > 0) creep.moveTo(standPos)
        } else {
            creep.moveTo(standPos)
        }
    }

    // Push fázis: kohézió + kite + heal+attack ugyanabban a tickben
    private fun pushBehavior(creep: Creep, gameplay: Gameplay) {
        val hostiles = gameplay.getHostileCreeps()

        // Társ assault megkeresése – kohézióhoz
        val ally = gameplay.myCreeps
            .firstOrNull { it.id != creep.id && it.role == Role.ASSAULT }
        val allyDist    = ally?.let { creep.getRangeTo(it) } ?: 0
        val waitForAlly = allyDist > MAX_COHESION_DIST

        // Lövés MINDIG lefut – heal már megtörtént execute()-ban, a kettő kombinálható
        shootBestTarget(creep, hostiles)

        val closest = hostiles.minByOrNull { creep.getRangeTo(it) }

        if (closest != null) {
            val dist = creep.getRangeTo(closest)
            when {
                // Melee range: azonnal kite
                dist <= 1 -> kiteAwayFromHostiles(creep, hostiles)

                // Túl közel, közeledik: visszahúzódik
                dist == 2 -> kiteAwayFromHostiles(creep, hostiles)

                // Ideális pozíció (3 range): helyben marad, lövés már megtörtént
                dist == 3 -> { /* áll */ }

                // Távolabb: közelít HA a társ is közel van
                else -> if (!waitForAlly) {
                    creep.moveTo(closest)
                } else {
                    // Társ bevárása: feléje megy
                    ally?.let { if (creep.getRangeTo(it) > 2) creep.moveTo(it) }
                }
            }
        } else {
            // Nincs ellenség: spawn felé, lövés a spawnra ha range-ben van
            val spawn = gameplay.getEnemySpawn() ?: return
            val distToSpawn = creep.getRangeTo(spawn)
            if (!waitForAlly) {
                when {
                    distToSpawn in 2..RANGED_ATTACK_RANGE -> {
                        if (creep.canRangedAttack()) creep.rangedAttack(spawn)
                    }
                    distToSpawn <= 1 -> {
                        if (creep.canRangedAttack()) creep.rangedAttack(spawn)
                        moveAwayFrom(creep, spawn)
                    }
                    else -> creep.moveTo(spawn)
                }
            } else {
                ally?.let { if (creep.getRangeTo(it) > 2) creep.moveTo(it) }
            }
        }
    }

    // Legjobb célpont lövése: 1-2 ellenség → rangedAttack a leggyengébbre, 3+ → massAttack
    private fun shootBestTarget(creep: Creep, hostiles: List<Creep>): Boolean {
        if (!creep.canRangedAttack()) return false
        val inRange = hostiles.filter { creep.getRangeTo(it) <= RANGED_ATTACK_RANGE }
        if (inRange.isEmpty()) return false
        if (inRange.size >= 3) creep.rangedMassAttack()
        else creep.rangedAttack(inRange.minByOrNull { it.hits }!!)
        return true
    }

    // Összes közeli ellenség súlypontjától távolodik
    private fun kiteAwayFromHostiles(creep: Creep, hostiles: List<Creep>) {
        val nearby = hostiles.filter { creep.getRangeTo(it) <= RANGED_ATTACK_RANGE + 1 }
        if (nearby.isEmpty()) return
        val avgX  = nearby.sumOf { it.x } / nearby.size
        val avgY  = nearby.sumOf { it.y } / nearby.size
        val dx    = creep.x - avgX
        val dy    = creep.y - avgY
        val moveX = if (dx == 0 && dy == 0) 1 else dx.coerceIn(-1, 1)
        val moveY = if (dx == 0 && dy == 0) 0 else dy.coerceIn(-1, 1)
        creep.moveTo(pos(
            (creep.x + moveX).coerceIn(1, 98),
            (creep.y + moveY).coerceIn(1, 98),
        ))
    }

    private fun moveAwayFrom(creep: Creep, target: GameObject) {
        val dx = creep.x - target.x
        val dy = creep.y - target.y
        creep.moveTo(pos(
            (creep.x + dx.coerceIn(-1, 1)).coerceIn(0, 99),
            (creep.y + dy.coerceIn(-1, 1)).coerceIn(0, 99),
        ))
    }
}