package it.unicam.cs.mpgc.rpg132007.world;

import it.unicam.cs.mpgc.rpg132007.action.DamageSkill;
import it.unicam.cs.mpgc.rpg132007.action.Effect;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.battle.AggressiveAI;
import it.unicam.cs.mpgc.rpg132007.battle.CautiousAI;
import it.unicam.cs.mpgc.rpg132007.battle.EnemyAI;
import it.unicam.cs.mpgc.rpg132007.battle.SpecialMoveAI;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.Element;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Reward;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import it.unicam.cs.mpgc.rpg132007.model.Status;

import java.util.List;
import java.util.function.Supplier;

import static it.unicam.cs.mpgc.rpg132007.model.Element.*;

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
    private static final int DEF_PER_STAGE = 3; // ogni 2 stage
    private static final double BOSS_HP = 1.4;
    private static final double BOSS_ATTACK = 1.0;
    private static final double ELITE_HP = 1.35;
    private static final double ELITE_ATTACK = 1.2;

    /** ai è un Supplier perché alcune IA hanno stato e ogni nemico deve averne una propria. */
    private record Template(String name, Appearance look, Element element, double hpMultiplier,
                            double attackMultiplier, Supplier<EnemyAI> ai) {
    }

    private static Template enemy(String name, String color, Element element, double hp, double atk,
                                  Supplier<EnemyAI> ai, String... sprite) {
        return new Template(name, new Appearance(List.of(sprite), color), element, hp, atk, ai);
    }

    private static Supplier<EnemyAI> special(String move, Element element, int powerPercent, int hits,
                                             boolean piercing, int every) {
        return special(move, element, powerPercent, hits, piercing, every, Effect.NONE);
    }

    private static Supplier<EnemyAI> special(String move, Element element, int powerPercent, int hits,
                                             boolean piercing, int every, Effect effect) {
        DamageSkill skill = new DamageSkill(move, 0, Skill.UNLIMITED, element, powerPercent, hits, piercing, "", effect);
        return () -> new SpecialMoveAI(skill, every);
    }

    private static final List<Template> NORMAL = List.of(
            enemy("Slime", "#7cfc00", ACQUA, 1.2, 0.8, AggressiveAI::new,
                    "",
                    "     .---.",
                    "   .' o o '.",
                    "  (   ___   )",
                    "   '._____.'"),
            enemy("Ratto Gigante", "#c8a27a", NEUTRO, 0.9, 1.0, AggressiveAI::new,
                    "   _   _",
                    "  (o)-(o)",
                    "   \\ Y /____",
                    "    `-'     )~~",
                    "     /_/-/_/"),
            enemy("Goblin", "#9acd32", NATURA, 1.0, 1.0, AggressiveAI::new,
                    "   ,      ,",
                    "  /(.-\"\"-.)\\",
                    "  \\  o  o  /",
                    "   \\  /\\  /",
                    "    '-==-'"),
            enemy("Pipistrello", "#b366ff", OMBRA, 0.8, 1.1, special("Morso Vampiro", OMBRA, 130, 1, false, 3, Effect.drain(50)),
                    "  __        __",
                    " /  \\_/\\_/\\_/  \\",
                    " \\___ (o o) ___/",
                    "     \\_\\v/_/"),
            enemy("Scheletro", "#e8e8e8", NEUTRO, 0.9, 1.1, CautiousAI::new,
                    "     .-.",
                    "    (o.o)",
                    "     |=|",
                    "    /|#|\\",
                    "     / \\"),
            enemy("Ragno Gigante", "#cc3333", NATURA, 1.0, 1.0, special("Morso Velenoso", NATURA, 80, 1, false, 3, Effect.poison(3)),
                    "   /\\  .-.  /\\",
                    "  //\\\\(o o)//\\\\",
                    "  /  \\(-v-)/  \\",
                    "     //   \\\\"),
            enemy("Lupo Mannaro", "#a0a0a0", NEUTRO, 1.1, 1.0, special("Artigli", NEUTRO, 75, 2, false, 2),
                    "    /\\___/\\",
                    "   ( o   o )",
                    "   (  =^=  )",
                    "   /|  w  |\\",
                    "    |_/ \\_|"),
            enemy("Fantasma", "#99ffff", OMBRA, 0.8, 1.1, special("Tocco Gelido", ACQUA, 90, 1, true, 3, Effect.inflict(Status.PARALISI, 3)),
                    "    .-\"\"-.",
                    "   / o  o \\",
                    "  |   __   |",
                    "  |  (__)  |",
                    "   \\/\\/\\/\\/"),
            enemy("Golem", "#b0a090", NATURA, 1.5, 0.8, CautiousAI::new,
                    "    [=====]",
                    "    | o o |",
                    "  __|  -  |__",
                    " |__|=====|__|",
                    "    |_| |_|"),
            enemy("Guardiano Sacro", "#ffe680", LUCE, 1.1, 1.0, special("Luce Accecante", LUCE, 110, 1, false, 3,
                            Effect.lower(Stat.ATTACK, 1)),
                    "     _/^\\_",
                    "    |(o o)|",
                    "    | -=- |",
                    "   /|_____|\\",
                    "     |   |"),
            enemy("Lich", "#9966ff", OMBRA, 0.9, 1.1, special("Raggio Oscuro", OMBRA, 130, 1, true, 3, Effect.lower(Stat.DEFENSE, 1)),
                    "     _/^\\_",
                    "    ( o o )",
                    "     \\ ~ /",
                    "   --|###|--",
                    "     /###\\"),
            enemy("Demone", "#ff4444", FUOCO, 1.0, 1.1, special("Fiammata", FUOCO, 120, 1, true, 3, Effect.inflict(Status.BRUCIATURA, 3)),
                    "   \\\\     //",
                    "    \\(o_o)/",
                    "     /|V|\\",
                    "    / |_| \\",
                    "     /   \\"),
            enemy("Cavaliere Nero", "#7f7fd5", OMBRA, 1.15, 1.05, special("Carica", NEUTRO, 190, 1, false, 3),
                    "     ,---.",
                    "    ( -=- )",
                    "    |[###]|--|>",
                    "    |_____|",
                    "     |   |"));

    private static final List<Template> BOSSES = List.of(
            enemy("Re Goblin", "#ffcc00", NATURA, 1.0, 1.0, special("Mazzata Reale", NATURA, 180, 1, false, 3),
                    "    \\^^^^^/",
                    "   ,|_____|,",
                    "  /( o   o )\\",
                    "  \\  \\___/  /",
                    "   '--===--'"),
            enemy("Orco Capo", "#ff8800", FUOCO, 1.1, 0.95, special("Terremoto", NATURA, 140, 1, false, 3, Effect.lower(Stat.DEFENSE, 1)),
                    "   \\ _____ /",
                    "    | > < |",
                    "    |  W  |",
                    "  /|_______|\\",
                    "    |  |  |"),
            enemy("Drago", "#ff3300", FUOCO, 1.05, 1.0, special("Soffio di Fuoco", FUOCO, 130, 1, false, 3, Effect.inflict(Status.BRUCIATURA, 3)),
                    "     __      __",
                    "    / \\\\----// \\",
                    "   (   o    o   )",
                    "    \\   /\\/\\   /",
                    "  ~~~\\________/~~~"));

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
                DEF_BASE + DEF_PER_STAGE * stage / 2);
        int rewardBoost = boss || elite ? 2 : 1;
        Reward reward = new Reward((12 + 6 * stage) * rewardBoost, (8 + 4 * stage) * (boss ? 3 : rewardBoost));
        String name = boss ? t.name() + " (BOSS)" : elite ? t.name() + " Élite" : t.name();
        return new Enemy(name, stage, stats, t.look(), reward, t.ai().get(), boss, t.element());
    }
}
