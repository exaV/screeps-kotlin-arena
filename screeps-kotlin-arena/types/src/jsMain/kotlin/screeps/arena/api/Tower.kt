@file:kotlin.js.JsModule("game/prototypes/tower")
@file:kotlin.js.JsNonModule

package screeps.arena.api

external open class StructureTower : OwnedStructure {
    var store: Store
    var cooldown: Double
    fun attack(target: dynamic): ScreepsReturnCode
    fun heal(target: Creep): ScreepsReturnCode
}
