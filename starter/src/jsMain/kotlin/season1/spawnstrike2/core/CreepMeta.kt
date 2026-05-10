package season1.spawnstrike2.core

import screeps.api.ATTACK
import screeps.api.Creep
import screeps.api.HEAL
import screeps.api.RANGED_ATTACK

enum class SpawnStrike2CreepKind {
    CTF,
    CTF_ESCORT,
    ALL_ROUNDER,
}

private val creepKindById = HashMap<String, SpawnStrike2CreepKind>()

var Creep.spawnStrike2Kind: SpawnStrike2CreepKind
    get() = creepKindById[id] ?: SpawnStrike2CreepKind.ALL_ROUNDER
    set(value) {
        creepKindById[id] = value
    }

fun Creep.canAttackBody(): Boolean =
    body.any { it.type == RANGED_ATTACK || it.type == ATTACK }

fun Creep.canHealBody(): Boolean =
    body.any { it.type == HEAL }
