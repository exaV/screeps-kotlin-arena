@file:JsModule("game/prototypes/flag")
@file:JsNonModule

package screeps.api

/** A flag is a key game object for this arena. Capture all flags to win the game. */
external open class Flag : GameObject {
    /** Equals to true or false if the flag is owned. Returns undefined if it is neutral. */
    val my: Boolean?
}
