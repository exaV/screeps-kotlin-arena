package season3.escortrun

import screeps.api.Creep

// ── Tick-szintű state tárolás ────────────────────────────────────────────────

internal val roleMap = mutableMapOf<String, Role>()
private val behaviorMap = mutableMapOf<String, Behavior>()

var Creep.role: Role
    get() = roleMap[id] ?: Role.FOLLOWER
    set(value) { roleMap[id] = value }

var Creep.behavior: Behavior
    get() = behaviorMap[id] ?: Behavior.FOLLOW
    set(value) { behaviorMap[id] = value }

fun Creep.hasRole(): Boolean = roleMap.containsKey(id)

// ── Combat FormationManager ───────────────────────────────────────────────────
// Harci keret: EscortRunStrategy.MAX_COMBAT_ALIVE – rally / deny szerint mozog

object CombatManager {
    private val combatCreeps = mutableListOf<Creep>()

    fun update(gameplay: Gameplay) {
        // Frissítjük az élő harci creepeket
        val alive = combatCreeps.filter { it.exists }.map { it.id }.toSet()
        combatCreeps.removeAll { !it.exists }

        // Új harci creepeket hozzáadjuk
        val newCombat = gameplay.myCreeps.filter {
            (it.role == Role.COMBAT_HYBRID ||
                it.role == Role.COMBAT_RANGER ||
                it.role == Role.COMBAT_FLAG_BLOCKER) &&
                it.id !in alive
        }
        combatCreeps.addAll(newCombat)
    }

    fun getCombatCreeps(): List<Creep> = combatCreeps.toList()
}

// ── Snake FormationManager ────────────────────────────────────────────────────
// MOVE_ONLY lánc + EscortCreep a vezető mögött (EscortRunStrategy.SNAKE_TOTAL)

object SnakeManager {
    // snakeOrder[0] = vezető MOVE_ONLY, snakeOrder[1] = EscortCreep (kezeljük külön),
    // snakeOrder[2..] = többi MOVE_ONLY
    private val snakeOrder = mutableListOf<Creep>()
    private var escortInserted = false

    fun update(gameplay: Gameplay) {
        // Halott tagok eltávolítása
        snakeOrder.removeAll { !it.exists }

        // Új MOVE_ONLY creepek hozzáadása a végére
        val existingIds = snakeOrder.map { it.id }.toSet()
        val newSnakeMembers = gameplay.myCreeps.filter {
            it.role == Role.SNAKE && it.id !in existingIds
        }
        snakeOrder.addAll(newSnakeMembers)

        // EscortCreep beillesztése a 2. helyre (egyszer)
        if (!escortInserted && snakeOrder.size >= 1) {
            val escort = gameplay.myEscortCreep
            if (escort != null) {
                // EscortCreep-et nem tesszük a snakeOrder-be, külön kezeljük
                // de jelöljük hogy már "beillesztettük"
                escortInserted = true
            }
        }
    }

    fun getLeader(): Creep? = snakeOrder.firstOrNull()

    // Az i-edik tag azt követi aki előtte van
    // snakeOrder[0] → flag
    // EscortCreep → snakeOrder[0]-t követi
    // snakeOrder[1] → EscortCreepet követi
    // snakeOrder[i] → snakeOrder[i-1]-et követi (i >= 2)
    fun getFollowTarget(creep: Creep, gameplay: Gameplay): Creep? {
        val index = snakeOrder.indexOf(creep)
        return when {
            index <= 0 -> null                              // vezető, nincs célpont
            index == 1 -> gameplay.myEscortCreep as? Creep // 2. tag az EscortCreepet követi
            else       -> snakeOrder[index - 1]
        }
    }

    fun getEscortFollowTarget(): Creep? = snakeOrder.firstOrNull() // EscortCreep a vezetőt követi

    fun getSnakeOrder(): List<Creep> = snakeOrder.toList()

    fun isEscortInserted(): Boolean = escortInserted
}