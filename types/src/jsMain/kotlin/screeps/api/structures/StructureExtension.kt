@file:JsModule("game/prototypes/extension")
@file:JsNonModule

package screeps.api.structures

import screeps.api.Store

/** Contains energy that can be spent on spawning bigger creeps. */
external open class StructureExtension : OwnedStructure {
    /** A {@link Store} object that contains cargo of this structure. */
    val store: Store
}
