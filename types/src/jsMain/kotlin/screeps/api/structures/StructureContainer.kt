@file:JsModule("game/prototypes/container")
@file:JsNonModule

package screeps.api.structures

import screeps.api.Store

/**
 * A small container that can be used to store resources.
 * This is a walkable structure.
 * All dropped resources automatically goes to the container at the same tile.
 */
external open class StructureContainer : OwnedStructure {
    /** A {@link Store} object that contains cargo of this structure. */
    val store: Store
}
