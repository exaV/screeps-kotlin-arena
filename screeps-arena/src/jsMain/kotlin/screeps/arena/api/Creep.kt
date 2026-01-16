@file:kotlin.js.JsModule("game/prototypes/creep")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally

@JsPlainObject
external interface BodyPartDefinition {
    var type: BodyPartType
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

    fun attack(target: dynamic): Int

    fun build(target: ConstructionSite): Int

    fun drop(resource: ResourceType, amount: Double = definedExternally): Int

    fun harvest(target: Source): Int

    fun heal(target: Creep): Int

    fun move(direction: Direction): Int

    fun moveTo(target: Position, options: FindPathOptions = definedExternally): Int

    fun pickup(target: Resource): Int

    fun pull(target: Creep): Int

    fun rangedAttack(target: dynamic): Int

    fun rangedHeal(target: Creep): Int

    fun rangedMassAttack(): Int

    fun transfer(target: dynamic, resource: ResourceType, amount: Double = definedExternally): Int

    fun withdraw(target: Structure, resource: ResourceType, amount: Double = definedExternally): Int
}
