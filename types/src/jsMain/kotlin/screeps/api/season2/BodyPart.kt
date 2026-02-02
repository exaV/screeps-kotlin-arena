@file:JsModule("arena/season_2/capture_the_flag/basic/prototypes")
@file:JsNonModule

package screeps.api.season2

import screeps.api.BodyPartType
import screeps.api.GameObject

/** A separate part of creep body */
external class BodyPart : GameObject {

    /** The type of the body part */
    val type: BodyPartType

}
