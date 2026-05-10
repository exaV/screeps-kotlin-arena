package season1.spawnstrike2.pipeline

import screeps.api.Creep
import season1.spawnstrike2.core.GamePhase
import season1.spawnstrike2.core.SpawnStrikePhases
import season1.spawnstrike2.core.SpawnStrike2CreepKind
import season1.spawnstrike2.core.WorldContext
import season1.spawnstrike2.core.canAttackBody
import season1.spawnstrike2.core.canHealBody
import season1.spawnstrike2.core.isEnemyInRange
import season1.spawnstrike2.core.spawnStrike2Kind

/**
 * Szabályok sorrendje = első találat nyer (mint a régi [season1.spawnstrike.Selector] lánc).
 * Új stratégia: adj hozzá egy [StrategyRule]-t ehhez a listához (sorrend számít).
 */
data class StrategyRule(
    val id: String,
    val matches: (WorldContext, Creep) -> Boolean,
    val behavior: CreepBehavior,
)

class RuleEngine(
    private val rules: List<StrategyRule>,
    private val fallback: CreepBehavior,
) {
    fun tick(world: WorldContext, creeps: Iterable<Creep>) {
        for (creep in creeps) {
            if (!creep.exists) continue
            val behavior = rules.firstOrNull { it.matches(world, creep) }?.behavior ?: fallback
            behavior.tick(world, creep)
        }
    }
}

object SpawnStrike2StrategyRulebook {

    private val captureMyFlag = MoveToMyFlagBehavior()
    private val captureEnemyFlag = MoveToEnemyFlagBehavior()
    private val escort = CtfEscortBehavior()
    private val defender = DefenderBehavior()
    private val heal = HealAlliesBehavior()
    private val attack = AttackEnemyBehavior()
    private val late = LateGameBehavior()
    private val neutral = NeutralGatherBehavior()

    fun defaultRules(): List<StrategyRule> = listOf(
        StrategyRule(
            id = "capture_my_flag",
            matches = { world, creep ->
                SpawnStrikePhases.current() < GamePhase.MIDDLE &&
                    creep.spawnStrike2Kind == SpawnStrike2CreepKind.CTF &&
                    !isEnemyInRange(creep, world.enemyCreeps, 5)
            },
            behavior = captureMyFlag,
        ),
        StrategyRule(
            id = "capture_enemy_flag",
            matches = { _, creep ->
                SpawnStrikePhases.current() < GamePhase.MIDDLE &&
                    creep.spawnStrike2Kind == SpawnStrike2CreepKind.CTF
            },
            behavior = captureEnemyFlag,
        ),
        StrategyRule(
            id = "ctf_escort_phase",
            matches = { _, _ -> SpawnStrikePhases.current() < GamePhase.MIDDLE },
            behavior = escort,
        ),
        StrategyRule(
            id = "defend_spawn",
            matches = { world, creep ->
                creep.canAttackBody() &&
                    SpawnStrikePhases.current() < GamePhase.LATE &&
                    isEnemyInRange(world.mySpawn, world.enemyCreeps, 15)
            },
            behavior = defender,
        ),
        StrategyRule(
            id = "heal_priority",
            matches = { world, creep ->
                if (!creep.canHealBody()) {
                    false
                } else {
                    val mine = world.myCreeps.filter { it.exists }
                    mine.any { it.hits != it.hitsMax } &&
                        (mine.maxByOrNull { it.hits } == creep || mine.size == 1)
                }
            },
            behavior = heal,
        ),
        StrategyRule(
            id = "attack_late",
            matches = { _, creep ->
                creep.canAttackBody() && SpawnStrikePhases.current() >= GamePhase.LATE
            },
            behavior = attack,
        ),
        StrategyRule(
            id = "late_game",
            matches = { _, creep ->
                creep.canAttackBody() && SpawnStrikePhases.current() >= GamePhase.LATE
            },
            behavior = late,
        ),
    )

    fun defaultEngine(): RuleEngine = RuleEngine(defaultRules(), neutral)
}
