package season3.escortrun

import screeps.api.*

// ── Execute extension – role + behavior kombinációja ────────────────────────

fun Creep.execute(gameplay: Gameplay) {
    when (role) {
        Role.LEADER    -> executeLeader(gameplay)
        Role.FOLLOWER  -> executeFollower(gameplay)
        Role.WORKER    -> executeWorker(gameplay)
        Role.HARVESTER -> executeHarvester(gameplay)
        Role.CARRIER   -> executeCarrier(gameplay)
    }
}

// ── Leader ───────────────────────────────────────────────────────────────────

private fun Creep.executeLeader(gameplay: Gameplay) {
    when (behavior) {
        Behavior.CAPTURE -> {
            val target = gameplay.getCaptureTarget()
            if (target != null) moveTo(target)
        }
        Behavior.RETREAT -> {
            moveTo(gameplay.mySpawn)
        }
        Behavior.ATTACK -> {
            val enemy = gameplay.getNearestEnemy(this) ?: return
            val dist = getRangeTo(enemy)

            if (dist <= 1) {
                attack(enemy)
                moveAwayFrom(enemy)
            } else if (dist <= 3 && canRangedAttack()) {
                rangedAttack(enemy)
                moveTo(enemy)
            } else {
                moveTo(enemy)
            }
        }
        else -> moveTo(gameplay.mySpawn)
    }
}

// ── Follower ─────────────────────────────────────────────────────────────────

private fun Creep.executeFollower(gameplay: Gameplay) {
    when (behavior) {
        Behavior.FOLLOW -> {
            val followTarget = FormationManager.getTarget(this) ?: return
            val dist = getRangeTo(followTarget)
            if (dist > 2) moveTo(followTarget)
            opportunisticAction(gameplay)
        }
        Behavior.FOCUS_FIRE -> {
            val focusTarget = gameplay.getFocusFireTarget() ?: return
            val dist = getRangeTo(focusTarget)
            if (dist <= 3 && canRangedAttack()) {
                rangedAttack(focusTarget)
            } else if (dist <= 1 && canAttack()) {
                attack(focusTarget)
                moveAwayFrom(focusTarget)
            } else {
                moveTo(focusTarget)
            }
        }
        Behavior.HEAL -> {
            val wounded = gameplay.getMostWounded() ?: return
            val dist = getRangeTo(wounded)
            if (dist <= 1) {
                heal(wounded)
            } else {
                moveTo(wounded)
                if (dist <= 3) rangedHeal(wounded)
            }
        }
        Behavior.RETREAT -> moveTo(gameplay.mySpawn)
        else -> {
            val followTarget = FormationManager.getTarget(this)
            if (followTarget != null) moveTo(followTarget)
        }
    }
}

// ── Worker ────────────────────────────────────────────────────────────────────
// Worker1: fix a source mellett (Harvester1 húzza oda), bányász + átad H1-nek
// Worker2: fix Worker1 mellett (Harvester1 húzza oda), bányász + átad W1-nek
// MOVE part nincs – csak harvest() és transfer() hívnak

private fun Creep.executeWorker(gameplay: Gameplay) {
    val w1 = EnergyChain.getPrimaryWorker(gameplay)
    val h1 = EnergyChain.getPrimaryHarvester(gameplay)
    val isW1 = (id == w1?.id)
    val source = gameplay.mySource

    if (isW1) {
        if (!EnergyChain.isWorker1InPlace(gameplay)) {
            // Még nincs helyén – pull párosításhoz moveTo(h1) kell
            // (MOVE part nélkül is szükséges a pull mechanikához)
            if (h1 != null) moveTo(h1)
            return
        }
        // Helyén van → bányász
        if (getRangeTo(source) <= 1) harvest(source)
        // Teli és H1 mellette → átad
        if (store.getUsedCapacity(RESOURCE_ENERGY) > 0 && h1 != null && getRangeTo(h1) <= 1) {
            transfer(h1, RESOURCE_ENERGY)
        }
    } else {
        if (!spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
            // Pull párosítás: minden ticken moveTo(h1) kell amíg nincs helyén
            if (h1 != null) moveTo(h1)
            return
        }
        // Helyén van → bányász
        if (getRangeTo(source) <= 1) harvest(source)
        // Teli és W1 mellette → átad
        if (store.getUsedCapacity(RESOURCE_ENERGY) > 0 && w1 != null && getRangeTo(w1) <= 1) {
            transfer(w1, RESOURCE_ENERGY)
        }
    }
}

// ── Harvester ─────────────────────────────────────────────────────────────────
// Harvester1: húzza a Workereket a source mellé, majd ingázik W1 ↔ H2/Spawn
// Harvester2: ingázik H1 ↔ Spawn

private fun Creep.executeHarvester(gameplay: Gameplay) {
    val h1 = EnergyChain.getPrimaryHarvester(gameplay)
    if (id == h1?.id) executeHarvester1(gameplay) else executeHarvester2(gameplay)
}

private fun Creep.executeHarvester1(gameplay: Gameplay) {
    val w1 = EnergyChain.getPrimaryWorker(gameplay) ?: return
    val w2 = EnergyChain.getSecondaryWorker(gameplay)
    val h2 = EnergyChain.getSecondaryHarvester(gameplay)
    val positions = EscortPositions.get(gameplay.mySpawn.y)

    // 1. Worker1 nincs helyén → húzd oda jump pozícióval
    if (!EnergyChain.isWorker1InPlace(gameplay)) {
        val jumpPos = positions.harvester1JumpForW1
        val w1Target = positions.worker1Target
        pull(w1)
        // Ha még nem értük el a jump pozíciót → menjünk oda
        // Ha már a jump pozíción vagyunk → lépjünk át a W1 célpozícióra
        if (x == jumpPos.x && y == jumpPos.y) {
            moveTo(w1Target)
        } else {
            moveTo(jumpPos)
        }
        return
    }

    // 2. Van Worker2, már létejött (nem spawning), és nincs helyén → húzd oda
    if (w2 != null && !w2.spawning && !EnergyChain.isWorker2InPlace(gameplay)) {
        val jumpPos  = positions.harvester1JumpForW2   // 4,2 – végső ugrás pozíció
        val approachPos = positions.worker2Target       // 3,2 – ahonnan ugrik

        if (getRangeTo(w2) > 1) {
            // Még nem értük el W2-t → menjünk hozzá
            moveTo(w2)
        } else {
            pull(w2)
            // Ha még nem értük el az approach pozíciót (3,2) → menjünk oda
            // Ha már ott vagyunk → ugorjunk a jumpPos-ra (4,2), W2 3,2-re kerül
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


// ── Carrier ───────────────────────────────────────────────────────────────────

private fun Creep.executeCarrier(gameplay: Gameplay) {
    // TODO
}

// ── Opportunista action hybrideknek ──────────────────────────────────────────

private fun Creep.opportunisticAction(gameplay: Gameplay) {
    if (canHeal()) {
        val wounded = gameplay.myCreeps
            .filter { it.hits < it.hitsMax && getRangeTo(it) <= 3 }
            .minByOrNull { it.hits }
        if (wounded != null) {
            if (getRangeTo(wounded) <= 1) heal(wounded) else rangedHeal(wounded)
            return
        }
    }
    if (canRangedAttack()) {
        val nearEnemy = gameplay.getEnemyCreeps()
            .filter { getRangeTo(it) <= 3 }
            .minByOrNull { it.hits }
        if (nearEnemy != null) rangedAttack(nearEnemy)
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