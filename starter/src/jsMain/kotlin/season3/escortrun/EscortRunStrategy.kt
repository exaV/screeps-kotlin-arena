package season3.escortrun

/**
 * Escort Run – stratégiai keret (közösségi / RTS „escort objective” elvek + a saját forrásbeli célprioritás-komment).
 *
 * **Cél:** a saját [EscortCreep] (VIP) eljuttatása a saját zászlóig, miközben az ellenfél VIP-jét és kíséretét megállítjuk.
 *
 * **Fázisok (spawn / makró):**
 * 1. **Gazdaság:** W1 → H1 → HYBRID (korai ranged+heal, védelem + nyomásgyakorlás).
 * 2. **Bővített gazdaság (tükör):** W2+H2 *csak* ha az ellenfél VIP még a saját spawnjánál – így nem késleltetjük feleslegesen a harci stacket, ha az ellenfél már „elengedte” a VIP-et (rush).
 * 3. **Harci keret:** max 8 élő HYBRID/RANGER – előbb erős tűzerő, utána hosszú MOVE-lánc (tipikus mirror / deny meta).
 * 4. **Kígyó:** 30× MOVE – útvonal a VIP-nek.
 * 5. **Utánpótlás:** RANGER/HYBRID váltás; ha az ellenséges VIP elhagyta a spawnját és még él,
 *    először harci HYBRID, utána SKIRMISHER-tömeg, további HYBRID slot nélkül (EscortRun).
 *
 * **Tűzvezérlés:** célprioritás [Gameplay.getPriorityTarget]; távolságok [season3.escortrun.combat.CombatTuning].
 */
object EscortRunStrategy {
    const val MAX_COMBAT_ALIVE: Int = 8
    const val SNAKE_TOTAL: Int = 30
    /** Saját komment: ellenséges VIP e távon belül a mi zászlónkhoz → ő a fő cél. */
    const val ENEMY_VIP_FLAG_DENY_RANGE: Int = 25

    /**
     * Ha az ellenséges VIP a mi [Gameplay.getCaptureTarget] célunkhoz képest még „versenyben” van
     * (nem a pálya túlsó végén), és közelebb van a célhoz, mint a legközelebbi harcosunk hozzá,
     * akkor is FOCUS legyen – ne maradjunk a spawn–zászló staging rally-n (pl. ~42% felé visszacsúszva).
     */
    const val VIP_RACE_CHASE_MAX_FLAG_DISTANCE: Int = 48
    /** Ellenséges VIP a spawnjánál (gazdaság-bővítés döntéshez). */
    const val ENEMY_VIP_NEAR_OWN_SPAWN_RANGE: Int = 5

    /** Alap gyülekező: spawn → zászló hányada (saját fél, nem a pálya közepe). */
    const val DEFAULT_RALLY_SPAWN_TO_FLAG_T: Double = 0.42

    // --- Korai opener: expansion vs gyors skirmisher ---

    /** Ha bármely ellenség ennél messzebb van a fő ellenséges spawn-tól → gyors MMRA opener. */
    const val ENEMY_LEFT_SPAWN_FOR_SKIRMISHER: Int = 20

    /** Távoli bővítő konténer (felső spawn meta; alsó: tükrözött Y). */
    const val EXPANSION_CONTAINER_X: Int = 92
    const val EXPANSION_CONTAINER_Y_TOP: Int = 49
}
