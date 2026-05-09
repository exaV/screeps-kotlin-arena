package season3.escortrun

import screeps.api.*

// ── Execute entry point ───────────────────────────────────────────────────────

fun Creep.execute(gameplay: Gameplay) {
    when (role) {
        Role.WORKER        -> executeWorker(gameplay)
        Role.HARVESTER     -> executeHarvester(gameplay)
        Role.COMBAT_HYBRID -> executeCombat(gameplay)
        Role.COMBAT_RANGER -> executeCombat(gameplay)
        Role.SNAKE         -> executeSnake(gameplay)
        else               -> {}
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

// ── Combat (HYBRID + RANGER) ──────────────────────────────────────────────────
// Fix a gyülekező ponton, ellenség közeledésekor támad

private fun Creep.executeCombat(gameplay: Gameplay) {
    when (behavior) {
        Behavior.WAIT -> {
            // Helyen vagyunk, opportunista lövés ha van közel ellenség
            opportunisticRangedAttack(gameplay)
        }
        Behavior.CAPTURE -> {
            // Gyülekező pontra megy
            val rally = gameplay.getCombatRallyPoint()
            if (getRangeTo(rally) > 2) moveTo(rally)
            opportunisticRangedAttack(gameplay)
        }
        Behavior.FOCUS_FIRE -> {
            val target = gameplay.getFocusFireTarget() ?: return
            val dist = getRangeTo(target)
            if (dist <= 3 && canRangedAttack()) {
                rangedAttack(target)
            } else if (dist <= 1 && canAttack()) {
                attack(target)
                moveAwayFrom(target)
            } else {
                moveTo(target)
            }
        }
        Behavior.HEAL -> {
            // Healel ha kell, különben lő
            val wounded = gameplay.getMostWounded()
            if (wounded != null && canHeal()) {
                val dist = getRangeTo(wounded)
                if (dist <= 1) heal(wounded)
                else if (dist <= 3) rangedHeal(wounded)
                else moveTo(wounded)
            } else {
                opportunisticRangedAttack(gameplay)
            }
        }
        else -> {}
    }
}

private fun Creep.opportunisticRangedAttack(gameplay: Gameplay) {
    if (!canRangedAttack()) return
    val nearEnemy = gameplay.getEnemyCreeps()
        .filter { getRangeTo(it) <= 3 }
        .minByOrNull { it.hits }
    if (nearEnemy != null) rangedAttack(nearEnemy)
}

// ── Snake (MOVE_ONLY) ─────────────────────────────────────────────────────────

private fun Creep.executeSnake(gameplay: Gameplay) {
    when (behavior) {
        Behavior.SNAKE_LEAD -> {
            // Vezető megy egyenesen a flagre
            val flag = gameplay.getCaptureTarget() ?: return
            if(getRangeTo(flag) > 3){
                moveTo(flag)
            }
        }
        Behavior.SNAKE_FOLLOW -> {
            // Követi az előtte lévőt, kígyózás: csak ha >2 távolság
            val followTarget = SnakeManager.getFollowTarget(this, gameplay) ?: return
            if (getRangeTo(followTarget) > 1) moveTo(followTarget)
        }
        else -> {}
    }
}

// ── Movement helper ───────────────────────────────────────────────────────────

private fun Creep.moveAwayFrom(target: GameObject) {
    val dx = x - target.x
    val dy = y - target.y
    val escapeX = (x + dx.coerceIn(-1, 1)).coerceIn(0, 99)
    val escapeY = (y + dy.coerceIn(-1, 1)).coerceIn(0, 99)
    moveTo(object : Position {
        override var x = escapeX
        override var y = escapeY
    })
}



