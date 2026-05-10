package season3.escortrun

import screeps.api.*
import season3.escortrun.combat.CombatExecutor

// ── Execute entry point ───────────────────────────────────────────────────────

fun Creep.execute(gameplay: Gameplay) {
    when (role) {
        Role.WORKER             -> executeWorker(gameplay)
        Role.HARVESTER          -> executeHarvester(gameplay)
        Role.COMBAT_HYBRID      -> executeCombat(gameplay)
        Role.COMBAT_RANGER      -> executeCombat(gameplay)
        Role.EXPANSION_BUILDER  -> ExpansionExecution.execute(this, gameplay)
        Role.SNAKE              -> executeSnake(gameplay)
        else                    -> {}
    }
}

// ── Worker ────────────────────────────────────────────────────────────────────

private fun Creep.executeWorker(gameplay: Gameplay) {
    val w1 = EnergyChain.getPrimaryWorker(gameplay)
    val h1 = EnergyChain.getPrimaryHarvester(gameplay)
    val isW1 = (id == w1?.id)
    val source = gameplay.mySource

    if (isW1) {
        if (!EnergyChain.isWorker1InPlace(gameplay)) {
            if (h1 != null) moveTo(h1)
            return
        }
        if (getRangeTo(source) <= 1) harvest(source)
        if (store.getUsedCapacity(RESOURCE_ENERGY) > 0 && h1 != null && getRangeTo(h1) <= 1) {
            transfer(h1, RESOURCE_ENERGY)
        }
    } else {
        if (!spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
            if (h1 != null) moveTo(h1)
            return
        }
        if (getRangeTo(source) <= 1) harvest(source)
        if (store.getUsedCapacity(RESOURCE_ENERGY) > 0 && w1 != null && getRangeTo(w1) <= 1) {
            transfer(w1, RESOURCE_ENERGY)
        }
    }
}

// ── Harvester ─────────────────────────────────────────────────────────────────

private fun Creep.executeHarvester(gameplay: Gameplay) {
    val h1 = EnergyChain.getPrimaryHarvester(gameplay)
    if (id == h1?.id) executeHarvester1(gameplay) else executeHarvester2(gameplay)
}

private fun Creep.executeHarvester1(gameplay: Gameplay) {
    val w1 = EnergyChain.getPrimaryWorker(gameplay) ?: return
    val w2 = EnergyChain.getSecondaryWorker(gameplay)
    val h2 = EnergyChain.getSecondaryHarvester(gameplay)
    val positions = EscortPositions.get(gameplay.mySpawn.y)

    // 1. Worker1 nincs helyén → húzd oda
    if (!EnergyChain.isWorker1InPlace(gameplay)) {
        val jumpPos  = positions.harvester1JumpForW1
        val w1Target = positions.worker1Target
        pull(w1)
        if (x == w1Target.x && y == w1Target.y) {
            moveTo(jumpPos)
        } else {
            moveTo(w1Target)
        }
        return
    }

    // 2. Van Worker2, már létrejött, nincs helyén → húzd oda
    if (w2 != null && !w2.spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
        val jumpPos     = positions.harvester1JumpForW2
        val approachPos = positions.worker2Target

        if (getRangeTo(w2) > 1) {
            moveTo(w2)
        } else {
            pull(w2)
            if (x == approachPos.x && y == approachPos.y) {
                moveTo(jumpPos)
            } else {
                moveTo(approachPos)
            }
        }
        return
    }

    // 3. Mindenki helyén → ingázás W1 ↔ H2/Spawn
    if (store.getUsedCapacity(RESOURCE_ENERGY) == 0) {
        if (getRangeTo(w1) > 1) moveTo(w1)
    } else {
        if (h2 != null) {
            if (getRangeTo(h2) <= 1) transfer(h2, RESOURCE_ENERGY) else moveTo(h2)
        } else {
            val spawn = gameplay.mySpawn
            if (getRangeTo(spawn) <= 1) transfer(spawn, RESOURCE_ENERGY) else moveTo(spawn)
        }
    }
}

private fun Creep.executeHarvester2(gameplay: Gameplay) {
    val h1 = EnergyChain.getPrimaryHarvester(gameplay) ?: return
    val spawn = gameplay.mySpawn

    if (store.getUsedCapacity(RESOURCE_ENERGY) == 0) {
        if (getRangeTo(h1) > 1) moveTo(h1)
    } else {
        if (getRangeTo(spawn) <= 1) transfer(spawn, RESOURCE_ENERGY) else moveTo(spawn)
    }
}

// ── Combat (HYBRID + RANGER) → [combat.CombatExecutor] ───────────────────────

private fun Creep.executeCombat(gameplay: Gameplay) {
    CombatExecutor.execute(this, gameplay)
}

// ── Snake (MOVE_ONLY) ─────────────────────────────────────────────────────────

private fun Creep.executeSnake(gameplay: Gameplay) {
    when (behavior) {
        Behavior.SNAKE_LEAD -> {
            val flag = gameplay.getCaptureTarget() ?: return
            val escort = gameplay.myEscortCreep

            // Ha a leader már 2 range-re van a flagtól → megáll, EscortCreep dolga a rest
            if (getRangeTo(flag) <= 2) return

            // Csak akkor megy előre ha az EscortCreep követi (max 4 range)
            val escortDist = if (escort != null) getRangeTo(escort) else 0
            if (escortDist <= 4) moveTo(flag)
        }
        Behavior.SNAKE_FOLLOW -> {
            val followTarget = SnakeManager.getFollowTarget(this, gameplay) ?: return
            if (getRangeTo(followTarget) > 1) moveTo(followTarget)
        }
        else -> {}
    }
}