@file:kotlin.js.JsModule("game/prototypes/tower")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external open class StructureTower : OwnedStructure {
    var store: Store
    var cooldown: Double
    fun attack(target: dynamic): Int
    fun heal(target: Creep): Int
}
