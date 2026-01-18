@file:kotlin.js.JsModule("game/prototypes/creep")
@file:kotlin.js.JsNonModule

package screeps.arena.api

import kotlinx.js.JsPlainObject
import kotlin.js.definedExternally

@JsPlainObject
external interface BodyPartDefinition {
    /** The body part type. */
    val type: BodyPartConstant
    /** The body part hits. */
    val hits: Int
}

/**
 * Creeps are units that can move, harvest energy, construct structures, attack another creeps, and perform other actions.
 * @extends GameObject
 */
external open class Creep : GameObject {
    /** An array describing the creep’s body. */
    val body: Array<BodyPartDefinition>

    /** The movement fatigue indicator. If it is greater than zero, the creep cannot move. */
    val fatigue: Int

    /** The current amount of hit points of the creep. */
    val hits: Int

    /** The maximum amount of hit points of the creep. */
    val hitsMax: Int

    /** Whether it is your creep. */
    val my: Boolean

    /** A {@link Store} object that contains cargo of this creep. */
    val store: Store

    /** Whether this creep is still being spawned. */
    val spawning: Boolean

    /**
     * Attack another creep, structure, or construction site in a short-ranged attack. Requires the {@link ATTACK} body part.
     * @param target The target object.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun attack(target: dynamic): ScreepsReturnCode

    /**
     * Build a structure at the target construction site using carried energy.
     * Requires {@link WORK} and {@link CARRY} body parts.
     * @param target The target construction site to be built.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun build(target: ConstructionSite): ScreepsReturnCode

    /**
     * Drop a resource on the ground.
     * @param resource One of the RESOURCE_* constants.
     * @param amount The amount of resource units to be dropped. If omitted, all the available carried amount is used.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun drop(resource: ResourceType, amount: Int = definedExternally): ScreepsReturnCode

    /**
     * Harvest energy from the source. Requires the {@link WORK} body part.
     * @param target The object to be harvested.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun harvest(target: Source): ScreepsReturnCode

    /**
     * Heal self or another creep nearby. Requires the {@link HEAL} body part.
     * @param target The target creep object.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun heal(target: Creep): ScreepsReturnCode

    /**
     * Move the creep one square in the specified direction. Requires the {@link MOVE} body part.
     * @param direction The direction to move.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun move(direction: Direction): ScreepsReturnCode

    /**
     * Find the optimal path to the target and move to it. Requires the {@link MOVE} body part.
     * @param target The target to move to. Can be a {@link GameObject} or any object containing x and y properties.
     * @param options An object with additional options that are passed to {@link findPath}.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun moveTo(target: Position, options: FindPathOptions = definedExternally): ScreepsReturnCode

    /**
     * Pick up an item (a dropped piece of resource). Requires the {@link CARRY} body part.
     * @param target The target object to be picked up.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun pickup(target: Resource): ScreepsReturnCode

    /**
     * Help another creep to follow this creep. Requires the {@link MOVE} body part.
     * @param target The target creep.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun pull(target: Creep): ScreepsReturnCode

    /**
     * A ranged attack against another creep or structure. Requires the {@link RANGED_ATTACK} body part.
     * @param target The target object to be attacked.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun rangedAttack(target: dynamic): ScreepsReturnCode

    /**
     * Heal another creep at a distance. Requires the {@link HEAL} body part.
     * @param target The target creep object.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun rangedHeal(target: Creep): ScreepsReturnCode

    /**
     * A ranged attack against all hostile creeps or structures within 3 squares range.  Requires the {@link RANGED_ATTACK} body part.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun rangedMassAttack(): ScreepsReturnCode

    /**
     * Transfer resource from the creep to another object.
     * @param target The target object.
     * @param resource One of the RESOURCE_* constants.
     * @param amount The amount of resources to be transferred. If omitted, all the available carried amount is used.
     */
    fun transfer(target: dynamic, resource: ResourceType, amount: Int = definedExternally): ScreepsReturnCode

    /**
     * Withdraw resources from a structure.
     * @param target The target structure.
     * @param resource One of the RESOURCE_* constants.
     * @param amount The amount of resources to be transferred. If omitted, all the available carried amount is used.
     * @returns Either {@link OK} or one of ERR_* error codes.
     */
    fun withdraw(target: Structure, resource: ResourceType, amount: Int = definedExternally): ScreepsReturnCode
}
