package it.unicam.cs.mpgc.rpg132007.enemies;

import it.unicam.cs.mpgc.rpg132007.core.Appearance;
import it.unicam.cs.mpgc.rpg132007.moves.DamageMove;

/**
 * Un tipo di nemico (come {@code HeroClass} per gli eroi): nome, aspetto, quanto è più o meno robusto della media
 * e la sua mossa speciale, usata ogni specialEvery turni. Le statistiche vere le calcola {@link StageEnemyFactory}
 * in base allo stage.
 */
public record EnemyType(String name, Appearance appearance, double hpMultiplier, double attackMultiplier,
                        DamageMove special, int specialEvery) {

    public EnemyType {
        if (hpMultiplier <= 0 || attackMultiplier <= 0 || specialEvery < 1) {
            throw new IllegalArgumentException("Tipo di nemico non valido: " + name);
        }
    }
}
