package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*
import sourcemaps.runWithSourceMapSupport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    runWithSourceMapSupport {
        initRoles()
        if (initialized) {
            SpawnStrike.tick()
        }
    }
}


// Spawns
val spawns = getObjectsByPrototype(StructureSpawn::class.js)

val mySpawn = spawns.first { it.my == true }
val enemySpawn = spawns.first { it.my == false }

// Creeps
val myCreeps: MutableList<Creep> = mutableListOf()

// Flags
val flags: List<Flag> = getObjectsByPrototype(Flag::class).filter { it.my == false }

// Roles
val tasks: MutableList<Role> = mutableListOf()

var initialized = false
fun initRoles() {
    if (!initialized) {
        initialized = true
        println("Init tasks.")

        val flags = flags.toMutableList()

        // create role CTF
        flags.toMutableList().map { flag ->
            Role(
                priority = 0,
                role = RoleType.CTF,
                requiredBodyParts = listOf(MOVE),
                process = { creep ->
                    findClosestByRange(creep, arrayOf(flag))
                        .also { creep.moveTo(flag) }
                }
            ).also { println("New role: ${it.role}.") }
        }.apply { tasks.addAll(this) }

        // create role warrior
        (1..2).map {
            Role(
                priority = 1,
                role = RoleType.WARRIOR,
                requiredBodyParts =
                    List(15) { MOVE } +
                            List(10) { RANGED_ATTACK } +
                            List(5) { HEAL },
                process = { creep ->

                    val lowestHealthFriendly = myCreeps
                        .filter { it.exists }
                        .filter { it.hits != it.hitsMax }
                        .minByOrNull { it.hits }

                    val enemyCreeps = getObjectsByPrototype(Creep::class).filter { !it.my }

                    val lowestHealthEnemy = enemyCreeps
                        .filter { it.exists }
                        .minByOrNull { it.hits }



                    if (lowestHealthFriendly?.hits != lowestHealthFriendly?.hitsMax) {
                        lowestHealthFriendly?.let {
                            if (creep == lowestHealthFriendly) {
                                // todo  saját magát valamiért nem healeli, ezt nem értem miért.
                                println("Attack: $lowestHealthEnemy")
                                attack(enemyCreeps, lowestHealthEnemy, creep)
                            } else {
                                if (creep.heal(lowestHealthFriendly) == ERR_NOT_IN_RANGE) {
                                    creep.moveTo(lowestHealthFriendly);
                                }
                            }
                        }
                    } else {
                        attack(enemyCreeps, lowestHealthEnemy, creep)
                    }
                }
            ).also { println("New role: ${it.role}.") }
        }.apply { tasks.addAll(this) }


        // create role ninja
        (1..2).map {
            Role(
                priority = 2,
                role = RoleType.NINJA,
                requiredBodyParts =
                    List(3) { RANGED_ATTACK } +
                            List(5) { MOVE } +
                            List(1) { TOUGH } +
                            List(1) { HEAL },
                process = { creep ->

                    val enemyCreeps = getObjectsByPrototype(Creep::class).filter { !it.my }

                    if (!enemyCreeps.isEmpty()) {
                        val lowestHealth = enemyCreeps
                            .filter { it.exists }
                            .minByOrNull { it.hits }

                        val targetCreep: Creep? = if (lowestHealth?.hits != lowestHealth?.hitsMax) {
                            lowestHealth
                        } else {
                            findClosestByRange(creep, enemyCreeps.toTypedArray())
                        }

                        if (targetCreep == null) {
                            println("Creep does not exist.")
                        } else {
                            if (creep.rangedAttack(targetCreep) == ERR_NOT_IN_RANGE) {
                                creep.moveTo(targetCreep);
                            }
                        }

                    } else {
                        if (creep.attack(enemySpawn) == ERR_NOT_IN_RANGE) {
                            creep.moveTo(enemySpawn);
                        }
                    }
                }
            ).also { println("New role: ${it.role}.") }
        }.apply { tasks.addAll(this) }

    }
}

private fun attack(
    enemyCreeps: List<Creep>,
    lowestHealthEnemy: Creep?,
    creep: Creep
) {
    if (!enemyCreeps.isEmpty()) {
        val targetArrackCreep: Creep? = if (lowestHealthEnemy?.hits != lowestHealthEnemy?.hitsMax) {
            lowestHealthEnemy
        } else {
            findClosestByRange(creep, enemyCreeps.toTypedArray())
        }


        // todo mass attack
        // todo findInRange?

        if (targetArrackCreep == null) {
            println("Creep does not exist.")
        } else {
            if (creep.rangedAttack(targetArrackCreep) == ERR_NOT_IN_RANGE) {
                creep.moveTo(targetArrackCreep);
            }
        }

        // todo kite

    } else {
        if (creep.rangedAttack(enemySpawn) == ERR_NOT_IN_RANGE) {
            creep.moveTo(enemySpawn);
        }
    }
}


object SpawnStrike {
    fun tick() {

        // liberate dead creeps
        tasks.forEach { task ->
            task.worker?.takeIf { it.hits == 0 }?.let { task.worker = null }
        }

        // spawn creep
        tasks.asSequence()
            .filter { it.worker == null }
            .minByOrNull { it.priority }
            ?.let { task ->
                spawnCreep(mySpawn, task.requiredBodyParts.toTypedArray())
                    ?.also { creep -> myCreeps.add(creep) }
                    ?.also { creep -> creep.delegateRole(tasks.firstOrNull { task -> task.worker == null }) }
                    ?.also { creep -> println("Creep spawned: $creep.") }
            }

        // let's work on the creep
        myCreeps.forEach { it.work() }
    }
}

private fun Creep.work() = this.task?.process()

data class Role(
    val role: RoleType,
    val priority: Int = 0,
    val requiredBodyParts: List<BodyPartType>,
    val process: (worker: Creep) -> Unit
) {
    var worker: Creep? = null

    fun process() = worker?.let { this.process(it) }
}


private val Creep.task: Role?
    get() = tasks.firstOrNull { it.worker == this }


private fun Creep.delegateRole(task: Role?) {
    println("Role: delegated.")
    task?.worker = this
}

// Basic functions
private fun spawnCreep(spawn: StructureSpawn, body: Array<BodyPartType>): Creep? {
    return spawn.spawnCreep(body).`object`
}

fun <T : Position> findClosestByRange(creep: Creep, positions: Array<T>): T {
    return creep.findClosestByRange(positions)
}

enum class RoleType {
    WARRIOR,
    CTF,
    NINJA,
}