package season1.spawnstrike

import screeps.api.Creep

object Scout {

    // Lent: 98,49 | Fent: 1,40
    private val BOTTOM_POS = pos(98, 49)
    private val TOP_POS    = pos(1, 40)

    fun execute(creep: Creep, gameplay: Gameplay) {
        val target = if (gameplay.isInTop()) TOP_POS else BOTTOM_POS
        if (creep.getRangeTo(target) > 0) creep.moveTo(target)
        // Ha megérkezett, nem csinál semmit
    }
}