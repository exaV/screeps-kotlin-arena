package season3.escortrun

import screeps.api.Creep

// ── Tick-szintű state tárolás ────────────────────────────────────────────────
// Az Arena nem perzisztál creep objektumokat tickek között,
// ezért modul szinten tároljuk az assignált role/behavior értékeket id alapján.

internal val roleMap = mutableMapOf<String, Role>()
private val behaviorMap = mutableMapOf<String, Behavior>()

var Creep.role: Role
    get() = roleMap[id] ?: Role.FOLLOWER
    set(value) { roleMap[id] = value }

var Creep.behavior: Behavior
    get() = behaviorMap[id] ?: Behavior.FOLLOW
    set(value) { behaviorMap[id] = value }

fun Creep.hasRole(): Boolean = roleMap.containsKey(id)

// ── Formation ────────────────────────────────────────────────────────────────

object FormationManager {

    // formationOrder[0] = Leader, formationOrder[i] követi formationOrder[i-1]-et
    private var formationOrder: List<Creep> = emptyList()

    fun initialize(creeps: List<Creep>) {
        if (formationOrder.isEmpty() && creeps.isNotEmpty()) {
            formationOrder = creeps.toList()
            formationOrder.first().role = Role.LEADER
            formationOrder.drop(1).forEach { it.role = Role.FOLLOWER }
        }
    }

    // Ha a leader meghal, a következő veszi át
    fun promoteIfNeeded() {
        formationOrder = formationOrder.filter { it.exists }
        if (formationOrder.isNotEmpty() && formationOrder.first().role != Role.LEADER) {
            formationOrder.first().role = Role.LEADER
        }
    }

    fun getLeader(): Creep? = formationOrder.firstOrNull()

    fun getTarget(follower: Creep): Creep? {
        val index = formationOrder.indexOf(follower)
        return if (index > 0) formationOrder[index - 1] else null
    }

    fun getFormationOrder(): List<Creep> = formationOrder
}