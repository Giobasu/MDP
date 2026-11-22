package it.unicam.cs.mpgc.rpg132007.enemies;

import it.unicam.cs.mpgc.rpg132007.core.Appearance;
import it.unicam.cs.mpgc.rpg132007.core.ResourceSprites;
import it.unicam.cs.mpgc.rpg132007.core.Stat;
import it.unicam.cs.mpgc.rpg132007.core.Status;
import it.unicam.cs.mpgc.rpg132007.moves.DamageMove;
import it.unicam.cs.mpgc.rpg132007.moves.Effect;

import java.util.List;

/**
 * Tutti i nemici del gioco base, un metodo per nemico (come {@code HeroClasses} per gli eroi).
 * Per aggiungerne uno basta scrivere un nuovo metodo, metterlo in {@link #normal} o {@link #bosses}
 * e mettere il suo sprite .txt accanto a questa classe nelle risorse.
 */
public final class EnemyTypes {

    /** I nemici normali, nell'ordine in cui si incontrano. */
    private final List<EnemyType> normal = List.of(spider(), warrior(), archer(), brute(), rider());
    /** I boss, uno ogni {@value StageEnemyFactory#BOSS_EVERY} stage. */
    private final List<EnemyType> bosses = List.of(general());

    public List<EnemyType> normal() {
        return normal;
    }

    public List<EnemyType> bosses() {
        return bosses;
    }

    private static EnemyType spider() {
        return enemy("Zigoton Ragno", "#cc3333", "spider.txt", 1.0, 1.0,
                move("Morso Velenoso", 80, 1, false, Effect.inflict(Status.POISON, 3)), 3);
    }

    private static EnemyType warrior() {
        return enemy("Zigoton Guerriero", "#d0d0d0", "warrior.txt", 1.1, 1.0,
                move("Doppio Taglio", 75, 2, false, Effect.NONE), 2);
    }

    private static EnemyType archer() {
        return enemy("Zigoton Arciere", "#9acd32", "archer.txt", 0.9, 1.1,
                move("Tiro Mirato", 130, 1, true, Effect.NONE), 3);
    }

    /** Tanti HP e poco attacco, ma il suo colpo pesante indebolisce la difesa dell'eroe. */
    private static EnemyType brute() {
        return enemy("Zigoton Bruto", "#b0a090", "brute.txt", 1.5, 0.8,
                move("Schianto", 150, 1, false, Effect.lower(Stat.DEFENSE, 1)), 4);
    }

    private static EnemyType rider() {
        return enemy("Zigoton Cavaliere", "#7f7fd5", "rider.txt", 1.15, 1.05,
                move("Carica", 190, 1, false, Effect.NONE), 3);
    }

    private static EnemyType general() {
        return enemy("Generale Zigoton", "#ff4444", "boss.txt", 1.0, 1.0,
                move("Colpo del Generale", 180, 1, false, Effect.lower(Stat.DEFENSE, 1)), 3);
    }

    /** sprite è il nome di un file .txt accanto a questa classe nelle risorse. */
    private static EnemyType enemy(String name, String color, String sprite, double hp, double atk,
                                   DamageMove special, int specialEvery) {
        return new EnemyType(name, new Appearance(ResourceSprites.load(EnemyTypes.class, sprite), color),
                hp, atk, special, specialEvery);
    }

    /** Le mosse dei nemici non hanno descrizione: il giocatore ne vede solo il nome. */
    private static DamageMove move(String name, int powerPercent, int hits, boolean piercing, Effect effect) {
        return new DamageMove(name, "", powerPercent, hits, piercing, effect);
    }
}
