@file:JsModule("game/prototypes/tower")
@file:JsNonModule

package screeps.api.structures

import screeps.api.Creep
import screeps.api.GameObject
import screeps.api.ScreepsReturnCode
import screeps.api.Store

/** Remotely attacks game objects or heals creeps within its range. */
external open class StructureTower : OwnedStructure {
    /** A {@link Store} object that contains cargo of this structure. */
    val store: Store
    /** The remaining amount of ticks while this tower cannot be used. */
    val cooldown: Int

    /**
     * Remotely attack any creep or structure in range.
     * @param target The target object.
     */
    fun attack(target: GameObject): ScreepsReturnCode

    /**
     * Remotely heal any creep in range.
     * @param target The target creep.
     */
    fun heal(target: Creep): ScreepsReturnCode
}
