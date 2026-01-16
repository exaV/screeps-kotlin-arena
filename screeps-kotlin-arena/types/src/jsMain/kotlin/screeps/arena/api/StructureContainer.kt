@file:kotlin.js.JsModule("game/prototypes/container")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/**
 * A small container that can be used to store resources.
 * This is a walkable structure.
 * All dropped resources automatically goes to the container at the same tile.
 */
external open class StructureContainer : OwnedStructure {
    /** A {@link Store} object that contains cargo of this structure. */
    var store: Store
}
