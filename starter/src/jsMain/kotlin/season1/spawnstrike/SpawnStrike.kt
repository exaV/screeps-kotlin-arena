package season1.spawnstrike

import screeps.api.*
import screeps.api.structures.*
import sourcemaps.runWithSourceMapSupport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    runWithSourceMapSupport {
        initTasks()
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

// Tasks
val tasks: MutableList<Task> = mutableListOf()

var initialized = false
fun initTasks() {
    if (!initialized) {
        initialized = true
        println("Init tasks.")

        // CTF tasks
        val flags = flags.toMutableList()
        flags.map { flag ->
            Task(
                priority = 1,
                type = TaskType.CTF,
                requiredBodyParts = listOf(MOVE, ATTACK),
                process = { creep ->
                    findClosestByRange(creep, arrayOf(flag))
                        .also { creep.moveTo(flag) }
                }
            ).also { println("New task: Capture the flag.") }
        }.apply { tasks.addAll(this) }

        // Todo: escort and defend the ctf-er creep

        // ATTACKER + HEALERS TASKS
        (1..30).map {

            /*
            *   1st Team Priority:
            *       Attacker: 3
            *       Healer: 4
            *
            *   2nd Team Priority:
            *       Attacker: 6
            *       Healer: 7
            *
            *   ...
             */
            val priority = it * 3

            // Healer
            when (it % 3 == 0) {
                true -> Task(
                    priority = priority + 1,
                    type = TaskType.HEALER,
                    requiredBodyParts = listOf(MOVE, HEAL),
                    process = { creep ->
                        val attackerCreeps = myCreeps.filter { it.task?.type == TaskType.ATTACKER }

                        attackerCreeps
                            .filter { it.exists }
                            .minByOrNull {
                                it.hits
                            }?.let {
                                val targetCreep = findClosestByRange(creep, arrayOf(it))
                                if (creep.heal(targetCreep) == ERR_NOT_IN_RANGE) {
                                    creep.moveTo(targetCreep);
                                }
                            }
                    }
                ).also { println("New task: Healer.") }

                // Attacker
                false -> Task(
                    priority = priority,
                    type = TaskType.ATTACKER,
                    requiredBodyParts = listOf(MOVE, ATTACK),
                    process = { creep ->
                        val enemyCreeps = getObjectsByPrototype(Creep::class).filter { !it.my }

                        if (!enemyCreeps.isEmpty()) {
                            val targetCreep = findClosestByRange(creep, enemyCreeps.toTypedArray())
                            if (creep.attack(targetCreep) == ERR_NOT_IN_RANGE) {
                                creep.moveTo(targetCreep);
                            }
                        } else {
                            if (creep.attack(enemySpawn) == ERR_NOT_IN_RANGE) {
                                creep.moveTo(enemySpawn);
                            }
                        }
                    }
                ).also { println("New task: Attacker.") }
            }
        }.apply { tasks.addAll(this) }

        // todo refactor: Priority to TaskType Enum.

        /*
         * ATTACKER -> 4,
         * HEALER -> 5,
         * CTF -> 1
         * CTF_ESCORT -> 2
         * SPAWN_DEFENDER -> 3 (delay until first attacker team)
         */
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
                    ?.also { creep -> creep.delegateTask(tasks.firstOrNull { task -> task.worker == null }) }
            }

        // let's work on the creep
        myCreeps.forEach { it.work() }
    }
}

private fun Creep.work() = this.task?.process()

data class Task(
    val type: TaskType,
    val priority: Int = 0,
    val requiredBodyParts: List<BodyPartType>,
    val process: (worker: Creep) -> Unit
) {
    var worker: Creep? = null

    fun process() = worker?.let { this.process(it) }
}


private val Creep.task: Task?
    get() = tasks.firstOrNull { it.worker == this }


private fun Creep.delegateTask(task: Task?) {
    println("Task: $task delegated, to: $this.")
    task?.worker = this
}

// Basic functions
private fun spawnCreep(spawn: StructureSpawn, body: Array<BodyPartType>): Creep? {
    println("Spawn creep with body: $body")
    return spawn.spawnCreep(body).`object`
}

fun <T : Position> findClosestByRange(creep: Creep, positions: Array<T>): T {
    return creep.findClosestByRange(positions)
}

enum class TaskType {
    ATTACKER,
    HEALER,
    CTF,
    CTF_ESCORT,
    SPAWN_DEFENDER
}