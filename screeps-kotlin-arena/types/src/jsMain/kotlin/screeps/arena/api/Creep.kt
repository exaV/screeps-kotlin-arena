@file:kotlin.js.JsModule("game/prototypes/creep")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally

@JsPlainObject
external interface BodyPartDefinition {
    var type: BodyPartConstant
    var hits: Double
}

external open class Creep : GameObject {
    var body: Array<BodyPartDefinition>
    var fatigue: Double
    var hits: Double
    var hitsMax: Double
    var my: Boolean
    var store: Store
    var spawning: Boolean

    fun attack(target: dynamic): ScreepsReturnCode

    fun build(target: ConstructionSite): ScreepsReturnCode

    fun drop(resource: ResourceType, amount: Double = definedExternally): ScreepsReturnCode

    fun harvest(target: Source): ScreepsReturnCode

    fun heal(target: Creep): ScreepsReturnCode

    fun move(direction: Direction): ScreepsReturnCode

    fun moveTo(target: Position, options: FindPathOptions = definedExternally): ScreepsReturnCode

    fun pickup(target: Resource): ScreepsReturnCode

    fun pull(target: Creep): ScreepsReturnCode

    fun rangedAttack(target: dynamic): ScreepsReturnCode

    fun rangedHeal(target: Creep): ScreepsReturnCode

    fun rangedMassAttack(): ScreepsReturnCode

    fun transfer(target: dynamic, resource: ResourceType, amount: Double = definedExternally): ScreepsReturnCode

    fun withdraw(target: Structure, resource: ResourceType, amount: Double = definedExternally): ScreepsReturnCode
}
