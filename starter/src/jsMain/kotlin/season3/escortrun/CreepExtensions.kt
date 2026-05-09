package season3.escortrun

import screeps.api.Creep
import screeps.api.ATTACK
import screeps.api.RANGED_ATTACK
import screeps.api.HEAL

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