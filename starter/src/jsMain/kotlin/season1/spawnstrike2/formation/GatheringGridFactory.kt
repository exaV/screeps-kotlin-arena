package season1.spawnstrike2.formation

/**
 * Koordináták a régi [season1.spawnstrike.SpawnStrikeGameplay]-ból.
 * Új térképekhez itt bővíthető / cserélhető.
 */
object GatheringGridFactory {

    fun buildCells(mySpawnY: Int): MutableList<GatheringCell> {
        val topY = listOf(12, 13, 14)
        val topX = 53..59
        val bottomY = listOf(85, 86, 87)
        val bottomX = 50..56
        val cells = mutableListOf<GatheringCell>()
        if (mySpawnY > 50) {
            for (y in bottomY) for (x in bottomX) cells.add(GatheringCell(x, y))
        } else {
            for (y in topY) for (x in topX) cells.add(GatheringCell(x, y))
        }
        return cells
    }
}
