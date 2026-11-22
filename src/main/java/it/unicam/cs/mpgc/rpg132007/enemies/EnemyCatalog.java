package it.unicam.cs.mpgc.rpg132007.enemies;

import java.util.List;

/**
 * I tipi di nemico disponibili (come {@code HeroClassCatalog} per gli eroi).
 * Oggi scritti nel codice ({@link EnemyTypes}), domani letti da file: {@link StageEnemyFactory} dipende solo da questa interfaccia.
 */
public interface EnemyCatalog {

    /** I nemici normali, nell'ordine in cui si incontrano. Almeno uno. */
    List<EnemyType> normal();

    /** I boss, nell'ordine in cui si incontrano. Almeno uno. */
    List<EnemyType> bosses();
}
