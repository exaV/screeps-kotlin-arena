@file:kotlin.js.JsModule("game/prototypes/extension")
@file:kotlin.js.JsNonModule

package screeps.arena.api

/** Contains energy that can be spent on spawning bigger creeps. */
external open class StructureExtension : OwnedStructure {
    /** A {@link Store} object that contains cargo of this structure. */
    var store: Store
}
