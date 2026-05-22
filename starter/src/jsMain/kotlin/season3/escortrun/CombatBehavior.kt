package season3.escortrun.combat

import screeps.api.*
import season3.escortrun.Gameplay
import season3.escortrun.Role
import season3.escortrun.canHeal
import season3.escortrun.canRangedAttack
import season3.escortrun.role

/**
 * Harci creepek (COMBAT_RANGER, COMBAT_HYBRID) viselkedése.
 *
 * Logika:
 *   1. Ha még nem értük el a gyülekező pontot → menjünk oda
 *   2. Ha megvagyunk a gyülekező ponton → maradjunk a csapatban
 *      - Ha van ellenség 5 range-en belül → támadd meg a legközelebbit
 *      - Ha nincs ellenség → tartsd a pozíciót (csapat centroidja körül)
 *   3. HYBRID: gyógyítsa a legkevesebb HP-s szövetségest ha kell
 */
object CombatBehavior {

    /** Ennyi range-en belüli ellenséget azonnal megtámadja. */
    private const val ENGAGE_RANGE = 5

    /** Rally pont elértnek tekinthető ennyi range-en belül. */
    private const val RALLY_REACHED_RANGE = 4

    /** Csapategység maximális szétszóródása – ennél távolabb ne maradjon senki. */
    private const val COHESION_MAX_SPREAD = 8

    fun execute(creep: Creep, gameplay: Gameplay) {
        val rally = gameplay.getCombatRallyPoint()

        // Gyógyítás (hybrid/healer) – mindig prioritás, mozgástól függetlenül
        if (creep.canHeal()) {
            healAllyIfNeeded(creep, gameplay)
            // Healer: tartsa magát a csapat közelében, ne menjen az ellenség felé
            val centroid = getTeamCentroid(gameplay)
            if (centroid != null && creep.getRangeTo(centroid) > COHESION_MAX_SPREAD) {
                creep.moveTo(centroid)
            } else if (creep.getRangeTo(rally) > RALLY_REACHED_RANGE) {
                creep.moveTo(rally)
            }
            return
        }

        // Ranger: ellenség 5 range-en belül → azonnal támadjuk
        val nearEnemy = gameplay.getHostileCreeps()
            .filter { creep.getRangeTo(it) <= ENGAGE_RANGE }
            .minByOrNull { creep.getRangeTo(it) }

        if (nearEnemy != null) {
            attackTarget(creep, nearEnemy)
            if (creep.getRangeTo(nearEnemy) > CombatTuning.RANGED_ATTACK_RANGE) {
                creep.moveTo(nearEnemy)
            }
            return
        }

        // Nincs ellenség közel – gyülekező pont / kohézió
        val distToRally = creep.getRangeTo(rally)
        if (distToRally > RALLY_REACHED_RANGE) {
            creep.moveTo(rally)
        } else {
            val centroid = getTeamCentroid(gameplay)
            if (centroid != null && creep.getRangeTo(centroid) > COHESION_MAX_SPREAD) {
                creep.moveTo(centroid)
            }
        }
    }

    // ── Ranged támadás ────────────────────────────────────────────────────────

    private fun attackTarget(creep: Creep, target: Creep) {
        if (creep.canRangedAttack()) {
            creep.rangedAttack(target)
        }
    }

    // ── Heal ──────────────────────────────────────────────────────────────────

    private fun healAllyIfNeeded(creep: Creep, gameplay: Gameplay) {
        val worstAlly = gameplay.myCreeps
            .filter { it.id != creep.id && it.hits < it.hitsMax * CombatTuning.ALLY_HEAL_HP_RATIO }
            .minByOrNull { it.hits }
            ?: return

        if (creep.getRangeTo(worstAlly) <= 1) {
            creep.heal(worstAlly)
        } else {
            creep.rangedHeal(worstAlly)
        }
    }

    // ── Csapat centroid ───────────────────────────────────────────────────────

    /**
     * A harci csapat (ranger + hybrid) átlagpozíciója.
     * Ide gravitál vissza aki túl messzire került.
     */
    private fun getTeamCentroid(gameplay: Gameplay): screeps.api.Position? {
        val fighters = gameplay.myCreeps.filter {
            it.role == Role.COMBAT_RANGER || it.role == Role.COMBAT_HYBRID
        }
        if (fighters.isEmpty()) return null
        val avgX = fighters.map { it.x }.average().toInt()
        val avgY = fighters.map { it.y }.average().toInt()
        return season3.escortrun.pos(avgX, avgY)
    }
}