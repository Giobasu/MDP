package it.unicam.cs.mpgc.rpg132007.world;

import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;

/** Crea il nemico di uno stage. Altre implementazioni possono leggere i nemici da file, generarli a caso, ecc. */
public interface EnemyFactory {

    Enemy create(int stage);

    /** Versione élite (più forte, ricompense più alte). Di base coincide con il nemico normale. */
    default Enemy createElite(int stage) {
        return create(stage);
    }

    /** Se lo stage è di un boss: prima dei boss non si sceglie il sentiero. */
    default boolean isBossStage(int stage) {
        return false;
    }
}
