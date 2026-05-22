package season3.escortrun

import screeps.api.*

/**
 * Minden ticken a még nem szereppel rendelkező creepeket besorolja
 * a testfelépítésük alapján.
 *
 * Sorrendben:
 *   1. ATTACK (move nélkül, csak 1 move) → COMBAT_DIGGER
 *   2. RANGED_ATTACK + HEAL → COMBAT_HYBRID
 *   3. RANGED_ATTACK (heal nélkül) → COMBAT_RANGER
 *   4. WORK → WORKER
 *   5. MOVE + CARRY (WORK nélkül) → HARVESTER
 */
object RoleAssigner {

    fun assign(gameplay: Gameplay) {
        val unassigned = gameplay.myCreeps
            .filter { !it.hasRole() }
            .sortedBy { it.id }

        for (creep in unassigned) {
            creep.role = detect(creep, gameplay)
        }
    }

    private fun detect(creep: Creep, gameplay: Gameplay): Role {
        val hasAttack = creep.body.any { it.type == ATTACK }
        val hasRanged = creep.canRangedAttack()
        val hasHeal   = creep.canHeal()
        val hasWork   = creep.body.any { it.type == WORK }

        return when {
            hasAttack             -> Role.COMBAT_DIGGER
            hasRanged && hasHeal  -> Role.COMBAT_HYBRID  // ha esetleg marad ilyen a jövőben
            hasHeal               -> Role.COMBAT_HYBRID
            hasRanged             -> Role.COMBAT_RANGER
            hasWork               -> Role.WORKER
            else                  -> Role.HARVESTER
        }
    }
}