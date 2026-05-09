package season3.escortrun

import screeps.api.Creep
import screeps.api.ATTACK
import screeps.api.RANGED_ATTACK
import screeps.api.HEAL

// ── Szerepek (ki vagy a csapatban) ──────────────────────────────────────────

enum class Role {
    LEADER,     // Formation élén, ő választ viselkedést
    FOLLOWER,   // Formation tagja, leadert követ
    WORKER,     // Statikus – bányász / builder
    HARVESTER,  // Statikus – erőforrás gyűjtő
    CARRIER,    // Statikus – szállítás spawn felé
}

// ── Viselkedések (mit csinálsz most) ────────────────────────────────────────

enum class Behavior {
    // Leader-only
    CAPTURE,    // Haladj a célpont felé (flag, pozíció)
    RETREAT,    // Visszavonulás a spawnhoz

    // Harci
    ATTACK,     // Legközelebbi ellenség
    FOCUS_FIRE, // Mindenki ugyanazt a targetet lövi
    HEAL,       // Szövetséges gyógyítása
    DEFEND,     // Pozíció tartása

    // Formation
    FOLLOW,     // Előtted lévőt kövesd a formationban
}

// ── Capability helpers ───────────────────────────────────────────────────────

fun Creep.canAttack(): Boolean =
    body.any { it.type == ATTACK || it.type == RANGED_ATTACK }

fun Creep.canHeal(): Boolean =
    body.any { it.type == HEAL }

fun Creep.canRangedAttack(): Boolean =
    body.any { it.type == RANGED_ATTACK }
