package season3.escortrun.combat

/**
 * Harci mikro-paraméterek – stratégia változtatásakor főleg itt állíts.
 * Makró (spawn, VIP deny távolság): [season3.escortrun.EscortRunStrategy].
 */
object CombatTuning {
    const val RANGED_ATTACK_RANGE: Int = 3

    /** Ennyire közeli ellenséget mindig a stratégiai cél elé vesszük (lövés / közeledés). */
    const val IMMEDIATE_THREAT_RANGE: Int = 8

    /** FlagBlockerAssigner: „már a VIP-re fókuszálunk” – harcos–VIP távolság ehhez képest is elég. */
    const val FLAG_BLOCKER_VIP_FOCUS_MAX_RANGE: Int = 10

    /**
     * Harcos: ha ennyire közel vagyunk a saját EscortCreep-hez, a VIP-t ténylegesen sebző ellenséget
     * választjuk (legalacsonyabb HP – stabil focus), még a saját 8-as „közeli fenyegetés” előtt is.
     */
    const val ESCORT_GUARD_FIGHTER_MAX_RANGE: Int = 22

    /** WAIT / CAPTURE közben is aktívan zárkózzunk fel, ha ellenség ennél belül van. */
    const val HOLD_AND_RALLY_ENGAGE_RANGE: Int = 8

    // --- Század-szintű „van-e már kontakt” (BehaviorSelector) ---

    const val COMBAT_AGGRO_RANGE: Int = 26
    const val ENEMY_PRESSURE_ON_SPAWN_RANGE: Int = 16
    const val ENEMY_NEAR_RALLY_RANGE: Int = 15
    const val ENEMY_CAMP_MAP_MID_RANGE: Int = 14

    /**
     * Ha ellenséges worker/carrier ennél messzebb van a mi spawnunktól, valószínűleg
     * távoli bővítés / második spawn – ne maradjunk „saját fél” rally-n passzívan.
     */
    const val ENEMY_DEEP_ECON_RAIDER_RANGE: Int = 40

    /** Ellenség e távon belül építkezéshez (ConstructionSite) kötődik – priorítás. */
    const val ENEMY_NEAR_HOSTILE_BUILD_RANGE: Int = 16

    /** Második spawn körüli ellenséges creepek priorítása. */
    const val ENEMY_NEAR_EXTRA_SPAWN_RANGE: Int = 20

    /** Gyülekező tolása az építkezés felé (spawn → cél hányados). */
    const val RALLY_TOWARD_THREAT_T: Double = 0.52

    // --- Heal ---

    /** Saját HP: ennél rosszabb → öngyógyítás (ha nincs fontosabb társ-heal). */
    const val SELF_HEAL_HP_RATIO: Double = 0.85

    /** Társ heal „azonnal”: csaknem teljes élet felett is (karc / 1 ütés után). */
    const val ALLY_HEAL_START_RATIO: Double = 0.998

    /** HEAL viselkedésben: ranged heal / heal célpont szűrése (régi 0.85 helyett). */
    const val ALLY_HEAL_HP_RATIO: Double = ALLY_HEAL_START_RATIO

    /**
     * Ha ennél jobb a saját HP-d, de melletted sérült a társ, ne foglald el a tick-et öngyógyítással.
     */
    const val SELF_HEAL_DEFER_TO_ALLY_ABOVE_RATIO: Double = 0.92

    // --- Harci csomó összetartás (rally a hybrid / centroid felé) ---

    /** Ha bármely két harcos távolsága nagyobb, húzzuk össze a rally-t. */
    const val COMBAT_COHESION_MAX_SPREAD: Int = 11

    /** Mennyire tolódjon a stratégiai rally a csomó közepe / hybrid felé (0..1). */
    const val COHESION_RALLY_BLEND_WEIGHT: Double = 0.55

    /** Van ilyen közel ellenség → ne szakítsuk meg a támadást összetartás miatt. */
    const val COHESION_SUSPEND_HOSTILE_RANGE: Int = 12
}
