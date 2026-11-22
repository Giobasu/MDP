package it.unicam.cs.mpgc.rpg132007.enemies;

import it.unicam.cs.mpgc.rpg132007.core.Stats;

import java.util.List;

/**
 * La curva di difficoltà: quale tipo di nemico appare a ogni stage e quanto è forte.
 * Ogni {@value #BOSS_EVERY} stage c'è un boss, negli altri un nemico normale; finiti i tipi del {@link EnemyCatalog}
 * si ricomincia dal primo, con statistiche sempre più alte. Chi siano i nemici lo decide il catalogo (di base {@link EnemyTypes}).
 */
public class StageEnemyFactory implements EnemyFactory {

    public static final int BOSS_EVERY = 5;

    // i valori sono tarati con BalanceTest perché ogni classe arrivi in fondo con fatica simile
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

    private final List<EnemyType> normal;
    private final List<EnemyType> bosses;

    /** Con i nemici del gioco base. */
    public StageEnemyFactory() {
        this(new EnemyTypes());
    }

    public StageEnemyFactory(EnemyCatalog types) {
        if (types.normal().isEmpty() || types.bosses().isEmpty()) {
            throw new IllegalArgumentException("Servono almeno un nemico normale e un boss");
        }
        this.normal = types.normal();
        this.bosses = types.bosses();
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
        EnemyType type = boss ? bossFor(stage) : normalFor(stage);
        return new Enemy(nameOf(type, boss, elite), stage, statsFor(type, stage, boss, elite), type.appearance(),
                rewardFor(stage, boss, elite), type.special(), type.specialEvery(), boss);
    }

    private EnemyType bossFor(int stage) {
        return bosses.get((stage / BOSS_EVERY - 1) % bosses.size());
    }

    /** Gli stage dei boss non contano: i nemici normali si susseguono senza saltarne nessuno. */
    private EnemyType normalFor(int stage) {
        return normal.get((stage - stage / BOSS_EVERY - 1) % normal.size());
    }

    private static Stats statsFor(EnemyType type, int stage, boolean boss, boolean elite) {
        double hpBoost = boss ? BOSS_HP : elite ? ELITE_HP : 1.0;
        double attackBoost = boss ? BOSS_ATTACK : elite ? ELITE_ATTACK : 1.0;
        return new Stats(
                (int) ((HP_BASE + HP_PER_STAGE * stage) * type.hpMultiplier() * hpBoost),
                (int) ((ATK_BASE + ATK_PER_STAGE * stage) * type.attackMultiplier() * attackBoost),
                DEF_BASE + DEF_PER_TWO_STAGES * stage / 2);
    }

    private static Reward rewardFor(int stage, boolean boss, boolean elite) {
        int rewardBoost = boss || elite ? BONUS_REWARD : 1;
        return new Reward((EXP_BASE + EXP_PER_STAGE * stage) * rewardBoost,
                (GOLD_BASE + GOLD_PER_STAGE * stage) * (boss ? BOSS_GOLD : rewardBoost));
    }

    private static String nameOf(EnemyType type, boolean boss, boolean elite) {
        return boss ? type.name() + " (BOSS)" : elite ? type.name() + " Élite" : type.name();
    }
}
