package com.github.exav.examples


import screeps.arena.api.*

fun tutorial1LoopAndImport() {
    println("hello world at ${getTicks()}")
}

fun tutorial2SimpleMove() {
    val creeps = getObjectsByPrototype(Creep::class.js)
    val flags = getObjectsByPrototype(Flag::class.js)

    creeps[0].moveTo(flags[0])
}

fun tutorial3FirstAttack() {
    val myCreep = getObjectsByPrototype(Creep::class.js).first { it.my }
    val enemyCreep = getObjectsByPrototype(Creep::class.js).first { !it.my }

    if (myCreep.attack(enemyCreep) == ERR_NOT_IN_RANGE) {
        myCreep.moveTo(enemyCreep);
    }
}

/**
 * ```typescript
 *     var myCreeps = getObjectsByPrototype(Creep).filter(creep => creep.my);
 *     var enemyCreep = getObjectsByPrototype(Creep).find(creep => !creep.my);
 *
 *     for(var creep of myCreeps) {
 *         if(creep.body.some(bodyPart => bodyPart.type == ATTACK)) {
 *             if(creep.attack(enemyCreep) == ERR_NOT_IN_RANGE) {
 *                 creep.moveTo(enemyCreep);
 *             }
 *         }
 *         if(creep.body.some(bodyPart => bodyPart.type == RANGED_ATTACK)) {
 *             if(creep.rangedAttack(enemyCreep) == ERR_NOT_IN_RANGE) {
 *                 creep.moveTo(enemyCreep);
 *             }
 *         }
 *         if(creep.body.some(bodyPart => bodyPart.type == HEAL)) {
 *             var myDamagedCreeps = myCreeps.filter(i => i.hits < i.hitsMax);
 *             if(myDamagedCreeps.length > 0) {
 *                 if(creep.heal(myDamagedCreeps[0]) == ERR_NOT_IN_RANGE) {
 *                     creep.moveTo(myDamagedCreeps[0]);
 *                 }
 *             }
 *         }
 *     }
 *  ```
 */
fun tutorial4CreepsBodies() {
    val myCreeps = getObjectsByPrototype(Creep::class.js).filter { it.my }
    val enemyCreep = getObjectsByPrototype(Creep::class.js).first { !it.my }

    for (creep in myCreeps) {
        if (creep.body.any { it.type == ATTACK }) {
            if (creep.attack(enemyCreep) == ERR_NOT_IN_RANGE) {
                creep.moveTo(enemyCreep);
            }

        }
        if (creep.body.any { it.type == RANGED_ATTACK }) {
            if (creep.rangedAttack(enemyCreep) == ERR_NOT_IN_RANGE) {
                creep.moveTo(enemyCreep);
            }
        }
        if (creep.body.any { it.type == HEAL }) {
            val myDamagedCreeps = myCreeps.filter { it.hits < it.hitsMax }
            if (myDamagedCreeps.isNotEmpty()) {
                if (creep.heal(myDamagedCreeps.first()) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(myDamagedCreeps.first());
                }
            }
        }
    }

}

/**
 * ```typescript
 *     const tower = utils.getObjectsByPrototype(prototypes.StructureTower)[0];
 *     if(tower.store[constants.RESOURCE_ENERGY] < 10) {
 *         var myCreep = utils.getObjectsByPrototype(prototypes.Creep).find(creep => creep.my);
 *         if(myCreep.store[constants.RESOURCE_ENERGY] == 0) {
 *             var container = utils.getObjectsByPrototype(prototypes.StructureContainer)[0];
 *             myCreep.withdraw(container, constants.RESOURCE_ENERGY);
 *         } else {
 *             myCreep.transfer(tower, constants.RESOURCE_ENERGY);
 *         }
 *     } else {
 *         var target = utils.getObjectsByPrototype(prototypes.Creep).find(creep => !creep.my);
 *         tower.attack(target);
 *     }
 * ```
 */
fun tutorial5StoreAndTransfer() {
    val tower = getObjectsByPrototype(StructureTower::class.js)[0]
    if (tower.store[RESOURCE_ENERGY] < 10) {
        val myCreep = getObjectsByPrototype(Creep::class.js).first { it.my }
        if (myCreep.store[RESOURCE_ENERGY] == 0.0) {
            val container = getObjectsByPrototype(StructureContainer::class.js)[0]
            myCreep.withdraw(container, RESOURCE_ENERGY);
        } else {
            myCreep.transfer(tower, RESOURCE_ENERGY);
        }
    } else {
        var target = getObjectsByPrototype(Creep::class.js).first { !it.my }
        tower.attack(target)
    }
}

/**
 * ```typescript
 *
 *     var creeps = getObjectsByPrototype(Creep).filter(i => i.my);
 *     var flags = getObjectsByPrototype(Flag);
 *     for(var creep of creeps) {
 *         var flag = creep.findClosestByPath(flags);
 *         creep.moveTo(flag);
 *     }
 * ```
 */
fun tutorial6Terrain() {
    val creeps = getObjectsByPrototype(Creep::class.js)
    val flags = getObjectsByPrototype(Flag::class.js)
    for (creep in creeps) {
        val flag = creep.findClosestByPath(flags)
        creep.moveTo(flag)
    }
}

var creep1: Creep? = null
var creep2: Creep? = null

/**
 * ```typescript
 *     var creeps = getObjectsByPrototype(Creep).filter(i => i.my);
 *     var flags = getObjectsByPrototype(Flag);
 *     for(var creep of creeps) {
 *         var flag = creep.findClosestByPath(flags);
 *         creep.moveTo(flag);
 *     }
 * ```
 */
fun tutorial7SpawnCreeps() {
    val spawn = getObjectsByPrototype(StructureSpawn::class.js)[0]
    val flags = getObjectsByPrototype(Flag::class.js)

    if (creep1 == null) {
        creep1 = spawn.spawnCreep(arrayOf(MOVE)).`object`
    } else {
        creep1?.moveTo(flags[0])

        if (creep2 == null) {
            creep2 = spawn.spawnCreep(arrayOf(MOVE)).`object`
        } else {
            creep2?.moveTo(flags[1])
        }
    }
}

/**
 * ```typescript
 *   var creep = utils.getObjectsByPrototype(prototypes.Creep).find(i => i.my);
 *     var source = utils.getObjectsByPrototype(prototypes.Source)[0];
 *     var spawn = utils.getObjectsByPrototype(prototypes.StructureSpawn).find(i => i.my);
 *
 *     if(creep.store.getFreeCapacity(constants.RESOURCE_ENERGY)) {
 *         if(creep.harvest(source) == constants.ERR_NOT_IN_RANGE) {
 *             creep.moveTo(source);
 *         }
 *     } else {
 *         if(creep.transfer(spawn, constants.RESOURCE_ENERGY) == constants.ERR_NOT_IN_RANGE) {
 *             creep.moveTo(spawn);
 *         }
 *     }
 * ```
 */

fun tutorial8HarvestEnergy() {
    val creep = getObjectsByPrototype(Creep::class.js).first { it.my }
    val source = getObjectsByPrototype(Source::class.js).first()
    val spawn = getObjectsByPrototype(StructureSpawn::class.js).first { it.my == true }

    if (creep.store.getFreeCapacity(RESOURCE_ENERGY) > 0) {
        if (creep.harvest(source) == ERR_NOT_IN_RANGE) {
            creep.moveTo(source)
        }
    } else {
        if (creep.transfer(spawn, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
            creep.moveTo(spawn)
        }
    }
}

/**
 * ```typescript
 *     const creep = utils.getObjectsByPrototype(prototypes.Creep).find(i => i.my);
 *     if(!creep.store[RESOURCE_ENERGY]) {
 *         const container = utils.findClosestByPath(creep, utils.getObjectsByPrototype(prototypes.StructureContainer));
 *         if(creep.withdraw(container, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
 *             creep.moveTo(container);
 *         }
 *     } else {
 *         const constructionSite = utils.getObjectsByPrototype(prototypes.ConstructionSite).find(i => i.my);
 *         if(!constructionSite) {
 *             utils.createConstructionSite(50,55, prototypes.StructureTower);
 *         } else {
 *             if(creep.build(constructionSite) == ERR_NOT_IN_RANGE) {
 *                 creep.moveTo(constructionSite);
 *             }
 *         }
 *     }
 *  ```
 */
fun tutorial9Construction() {
    val creep = getObjectsByPrototype(Creep::class.js).first { it.my }
    if (creep.store[RESOURCE_ENERGY] == 0.0) {
        val container = findClosestByPath(creep, getObjectsByPrototype(StructureContainer::class.js))
        if (creep.withdraw(container, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
            creep.moveTo(container)
        }
    } else {
        val consructionSite = getObjectsByPrototype(ConstructionSite::class.js).firstOrNull() { it.my == true }
        if (consructionSite == null) {
            createConstructionSite(50.0, 55.0, StructureTower::class.js)
        } else {
            if (creep.build(consructionSite) == ERR_NOT_IN_RANGE) {
                creep.moveTo(consructionSite)
            }
        }
    }
}

object Tutorials10FinalTest {


    fun run() {
        val source = getObjectsByPrototype(Source::class.js).first()
        val spawn = getObjectsByPrototype(StructureSpawn::class.js).first { it.my == true }

        val myCreeps = getObjectsByPrototype(Creep::class.js).filter { it.my }
        val enemies = getObjectsByPrototype(Creep::class.js).filterNot { it.my }

        val workers = myCreeps.filter { it.body.any { it.type == WORK } }
        val attackers = myCreeps.filter { it.body.any { it.type == ATTACK } }

        if (workers.size < 2){
            spawn.spawnCreep(arrayOf(WORK, MOVE, CARRY))
        } else if (attackers.size < 10){
            spawn.spawnCreep(arrayOf(MOVE, ATTACK))
        }

        for (creep in workers){
            if (creep.store.getFreeCapacity(RESOURCE_ENERGY) > 0){
                if (creep.harvest(source) == ERR_NOT_IN_RANGE){
                    creep.moveTo(source)
                }
            } else {
                if (creep.transfer(spawn, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE){
                    creep.moveTo(spawn)
                }
            }

        }
        for (creep in attackers) {
            if (enemies.isNotEmpty()){
                val target = creep.findClosestByPath(enemies.toTypedArray())
                if (creep.attack(target) == ERR_NOT_IN_RANGE){
                    creep.moveTo(target)
                }

                when(val code = creep.attack(target)){
                    ERR_NOT_IN_RANGE -> creep.moveTo(target)
                    OK -> println("${creep.id}: ${target.id} get off my land!")
                    ERR_INVALID_TARGET -> {} // ignore
                    else -> println("error $code")
                }
            }

        }
    }

}

