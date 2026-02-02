@file:JsModule("arena/season_2/capture_the_flag/basic/prototypes")
@file:JsNonModule

package screeps.api.season2

import screeps.api.BodyPartType
import screeps.api.Flag

/** A separate part of creep body */
external class BonusFlag : Flag {

    /** The type of the body part */
    val bonusType: BodyPartType

}
