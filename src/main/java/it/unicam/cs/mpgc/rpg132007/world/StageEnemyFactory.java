package it.unicam.cs.mpgc.rpg132007.world;

import it.unicam.cs.mpgc.rpg132007.action.DamageMove;
import it.unicam.cs.mpgc.rpg132007.action.Effect;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.ResourceSprites;
import it.unicam.cs.mpgc.rpg132007.model.Reward;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import it.unicam.cs.mpgc.rpg132007.model.Status;

import java.util.List;

/**
 * Ogni {@value #BOSS_EVERY} stage c'è un boss, negli altri un nemico normale.
 * Finiti i modelli si ricomincia dal primo, con statistiche sempre più alte.
 */
public class StageEnemyFactory implements EnemyFactory {

    public static final int BOSS_EVERY = 5;

    // curva di difficoltà: i valori sono tarati con BalanceTest perché ogni classe arrivi in fondo con fatica simile
    private static final int HP_BASE = 34;
    private static final int HP_PER_STAGE = 15;
    private static final int ATK_BASE = 10;
    private static final double ATK_PER_STAGE = 3.0;
    private static final int DEF_BASE = 2;
    private static final int DEF_PER_TWO_STAGES = 3;
    private static final double BOSS_HP = 1.4;
    private static final double BOSS_ATTACK = 1.0;
    private static final double ELITE_HP = 1.35;
    private static final double ELITE_ATTACK = 1.2;
    private static final int EXP_BASE = 12;
    private static final int EXP_PER_STAGE = 6;
    private static final int GOLD_BASE = 8;
    private static final int GOLD_PER_STAGE = 4;
    /** Élite e boss danno EXP doppia; l'élite anche oro doppio. */
    private static final int BONUS_REWARD = 2;
    /** I boss danno il triplo dell'oro. */
    private static final int BOSS_GOLD = 3;

    /** Un modello di nemico: aspetto, moltiplicatori delle statistiche e mossa speciale (usata ogni specialEvery turni). */
    private record Template(String name, Appearance look, double hpMultiplier, double attackMultiplier,
                            DamageMove special, int specialEvery) {
    }

    /** sprite è il nome di un file .txt accanto a questa classe nelle risorse. */
    private static Template enemy(String name, String color, String sprite, double hp, double atk,
                                  DamageMove special, int specialEvery) {
        return new Template(name, new Appearance(ResourceSprites.load(StageEnemyFactory.class, sprite), color),
                hp, atk, special, specialEvery);
    }

    private static DamageMove move(String name, int powerPercent, int hits, boolean piercing, Effect effect) {
        return new DamageMove(name, "", powerPercent, hits, piercing, effect);
    }

    /** I nemici normali, nell'ordine in cui si incontrano. */
    private static final List<Template> NORMAL = List.of(spider(), warrior(), archer(), brute(), rider());

    private static final List<Template> BOSSES = List.of(general());

    private static Template spider() {
        return enemy("Zigoton Ragno", "#cc3333", "spider.txt", 1.0, 1.0,
                move("Morso Velenoso", 80, 1, false, Effect.inflict(Status.POISON, 3)), 3);
    }

    private static Template warrior() {
        return enemy("Zigoton Guerriero", "#d0d0d0", "warrior.txt", 1.1, 1.0,
                move("Doppio Taglio", 75, 2, false, Effect.NONE), 2);
    }

    private static Template archer() {
        return enemy("Zigoton Arciere", "#9acd32", "archer.txt", 0.9, 1.1,
                move("Tiro Mirato", 130, 1, true, Effect.NONE), 3);
    }

    /** Tanti HP e poco attacco, ma il suo colpo pesante indebolisce la difesa dell'eroe. */
    private static Template brute() {
        return enemy("Zigoton Bruto", "#b0a090", "brute.txt", 1.5, 0.8,
                move("Schianto", 150, 1, false, Effect.lower(Stat.DEFENSE, 1)), 4);
    }

    private static Template rider() {
        return enemy("Zigoton Cavaliere", "#7f7fd5", "rider.txt", 1.15, 1.05,
                move("Carica", 190, 1, false, Effect.NONE), 3);
    }

    private static Template general() {
        return enemy("Generale Zigoton", "#ff4444", "boss.txt", 1.0, 1.0,
                move("Colpo del Generale", 180, 1, false, Effect.lower(Stat.DEFENSE, 1)), 3);
    }

    @Override
    public boolean isBossStage(int stage) {
        return stage % BOSS_EVERY == 0;
    }

    @Override
    public Enemy create(int stage) {
        return create(stage, false);
    }

    /** Sui boss l'élite non ha senso: si ottiene sempre il boss normale. */
    @Override
    public Enemy createElite(int stage) {
        return create(stage, !isBossStage(stage));
    }

    private Enemy create(int stage, boolean elite) {
        if (stage < 1) {
            throw new IllegalArgumentException("Stage non valido: " + stage);
        }
        boolean boss = isBossStage(stage);
        Template t = boss
                ? BOSSES.get((stage / BOSS_EVERY - 1) % BOSSES.size())
                : NORMAL.get((stage - stage / BOSS_EVERY - 1) % NORMAL.size());
        double hpBoost = boss ? BOSS_HP : elite ? ELITE_HP : 1.0;
        double attackBoost = boss ? BOSS_ATTACK : elite ? ELITE_ATTACK : 1.0;
        Stats stats = new Stats(
                (int) ((HP_BASE + HP_PER_STAGE * stage) * t.hpMultiplier() * hpBoost),
                (int) ((ATK_BASE + ATK_PER_STAGE * stage) * t.attackMultiplier() * attackBoost),
                DEF_BASE + DEF_PER_TWO_STAGES * stage / 2);
        int rewardBoost = boss || elite ? BONUS_REWARD : 1;
        Reward reward = new Reward((EXP_BASE + EXP_PER_STAGE * stage) * rewardBoost,
                (GOLD_BASE + GOLD_PER_STAGE * stage) * (boss ? BOSS_GOLD : rewardBoost));
        String name = boss ? t.name() + " (BOSS)" : elite ? t.name() + " Élite" : t.name();
        return new Enemy(name, stage, stats, t.look(), reward, t.special(), t.specialEvery(), boss);
    }
}
