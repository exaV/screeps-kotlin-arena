package season3.escortrun

import screeps.api.CARRY
import screeps.api.Creep
import screeps.api.ATTACK
import screeps.api.MOVE
import screeps.api.RANGED_ATTACK
import screeps.api.HEAL
import screeps.api.WORK

// ── Szerepek ─────────────────────────────────────────────────────────────────

enum class Role {
    // Statikus gazdasági szerepek
    WORKER,
    HARVESTER,
    CARRIER,

    // Harci csapat (fix középen)
    COMBAT_HYBRID,   // MOVE, MOVE, RANGED_ATTACK, HEAL
    COMBAT_RANGER,   // MOVE, MOVE, MOVE, RANGED_ATTACK x3

    // Kígyó
    SNAKE,           // MOVE_ONLY – kígyó tagja

    /** MOVE×2 WORK×2 CARRY – távoli konténer + második spawn építés. */
    EXPANSION_BUILDER,

    // Régi formation (megtartjuk kompatibilitáshoz)
    LEADER,
    FOLLOWER,
}

// ── Viselkedések ─────────────────────────────────────────────────────────────

enum class Behavior {
    CAPTURE,
    RETREAT,
    ATTACK,
    FOCUS_FIRE,
    HEAL,
    DEFEND,
    FOLLOW,
    WAIT,        // Várakozás gyülekező ponton
    SNAKE_LEAD,  // Kígyó vezető – megy a flagre
    SNAKE_FOLLOW,// Kígyó tag – követi az előtte lévőt
}

// ── Capability helpers ────────────────────────────────────────────────────────

fun Creep.canAttack(): Boolean =
    body.any { it.type == ATTACK || it.type == RANGED_ATTACK }

fun Creep.canHeal(): Boolean =
    body.any { it.type == HEAL }

fun Creep.canRangedAttack(): Boolean =
    body.any { it.type == RANGED_ATTACK }

fun Creep.isExpansionRunnerBody(): Boolean {
    if (body.size != 5) return false
    var m = 0
    var w = 0
    var c = 0
    for (p in body) {
        when (p.type) {
            MOVE -> m++
            WORK -> w++
            CARRY -> c++
            else -> return false
        }
    }
    return m == 2 && w == 2 && c == 1
}