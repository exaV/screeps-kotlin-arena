package season3.escortrun.combat

import screeps.api.CARRY
import screeps.api.Creep
import screeps.api.Position
import screeps.api.WORK
import season3.escortrun.EscortRunStrategy
import season3.escortrun.Gameplay

/** Század-szintű: érdemes-e már „harcban” kezelni a harci creepeket (FOCUS), ne rally-menüben. */
object SquadContact {

    fun globalFightStance(
        gameplay: Gameplay,
        combatCreeps: List<Creep>,
        allHostiles: List<Creep>,
        rallyPoint: Position,
        mapMid: Position,
    ): Boolean {
        val flag = gameplay.getCaptureTarget()
        val enemyVip = allHostiles.firstOrNull { it.hitsMax == 5000 }
        if (flag != null && enemyVip != null &&
            enemyVip.getRangeTo(flag) <= EscortRunStrategy.ENEMY_VIP_FLAG_DENY_RANGE
        ) {
            return true
        }
        val mySpawn = gameplay.mySpawn

        if (gameplay.getHostileConstructionSites().isNotEmpty()) {
            return true
        }
        if (gameplay.getEnemySpawns().size >= 2) {
            return true
        }
        val deepEconRaider = allHostiles.any { enemy ->
            enemy.hitsMax != 5000 &&
                enemy.getRangeTo(mySpawn) >= CombatTuning.ENEMY_DEEP_ECON_RAIDER_RANGE &&
                enemy.body.any { p -> p.type == WORK || p.type == CARRY }
        }
        if (deepEconRaider) {
            return true
        }

        if (allHostiles.any { it.getRangeTo(mySpawn) <= CombatTuning.ENEMY_PRESSURE_ON_SPAWN_RANGE }) {
            return true
        }
        if (allHostiles.any { it.getRangeTo(rallyPoint) <= CombatTuning.ENEMY_NEAR_RALLY_RANGE }) {
            return true
        }
        if (allHostiles.any { it.getRangeTo(mapMid) <= CombatTuning.ENEMY_CAMP_MAP_MID_RANGE }) {
            return true
        }
        if (allHostiles.any { enemy ->
                combatCreeps.any { it.getRangeTo(enemy) <= CombatTuning.COMBAT_AGGRO_RANGE }
            }
        ) {
            return true
        }
        return false
    }
}
