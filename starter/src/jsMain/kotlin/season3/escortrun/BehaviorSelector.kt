package season3.escortrun

import screeps.api.Creep
import screeps.api.HEAL_POWER

// ── Heal deficit számítás ────────────────────────────────────────────────────

private fun calcHealDeficit(creeps: List<Creep>): Int =
    creeps.sumOf { (it.hitsMax - it.hits).coerceAtLeast(0) }

private fun calcHealersNeeded(creeps: List<Creep>): Int {
    val deficit = calcHealDeficit(creeps)
    if (deficit == 0) return 0
    return (deficit / HEAL_POWER).coerceAtLeast(1)
}

// ── BehaviorSelector ─────────────────────────────────────────────────────────

object BehaviorSelector {

    fun assignAll(gameplay: Gameplay) {
        val leader = FormationManager.getLeader() ?: return
        val followers = FormationManager.getFormationOrder().drop(1)

        val leaderBehavior = selectLeaderBehavior(leader, gameplay)
        leader.behavior = leaderBehavior

        assignFollowerBehaviors(followers, leaderBehavior, gameplay)
    }

    private fun selectLeaderBehavior(leader: Creep, gameplay: Gameplay): Behavior {
        if (leader.hits < leader.hitsMax * 0.3) return Behavior.RETREAT

        val enemies = gameplay.getEnemyCreeps()
        if (enemies.any { leader.getRangeTo(it) <= 5 }) return Behavior.ATTACK

        return Behavior.CAPTURE
    }

    private fun assignFollowerBehaviors(
        followers: List<Creep>,
        leaderBehavior: Behavior,
        gameplay: Gameplay
    ) {
        when (leaderBehavior) {
            Behavior.CAPTURE -> followers.forEach { it.behavior = Behavior.FOLLOW }

            Behavior.RETREAT -> {
                val leader = FormationManager.getLeader()
                if (leader != null && leader.hits < leader.hitsMax * 0.15) {
                    followers.forEach { it.behavior = Behavior.RETREAT }
                } else {
                    followers.forEach { it.behavior = Behavior.FOLLOW }
                }
            }

            Behavior.ATTACK -> {
                val healersNeeded = calcHealersNeeded(gameplay.myCreeps)
                var assignedHealers = 0

                for (follower in followers) {
                    if (follower.canHeal() && assignedHealers < healersNeeded) {
                        follower.behavior = Behavior.HEAL
                        assignedHealers++
                    } else if (follower.canAttack()) {
                        follower.behavior = Behavior.FOCUS_FIRE
                    } else {
                        follower.behavior = Behavior.FOLLOW
                    }
                }
            }

            else -> followers.forEach { it.behavior = Behavior.FOLLOW }
        }
    }
}