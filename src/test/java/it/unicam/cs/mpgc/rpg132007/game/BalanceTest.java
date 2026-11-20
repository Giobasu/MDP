package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.DamageSkill;
import it.unicam.cs.mpgc.rpg132007.action.DrinkPotion;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.action.StatusSkill;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Element;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Hero;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveData;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg132007.world.StageEnemyFactory;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simula una partita intera per ogni classe con un giocatore "ragionevole" (non perfetto)
 * e controlla che nessuna classe sia molto più forte delle altre e che la difficoltà salga senza muri.
 */
class BalanceTest {

    private static final int BATTLES = 70;
    private static final boolean DEBUG = Boolean.getBoolean("balance.debug");

    private record Result(int stage, int level, int defeats, int turns) {
    }

    private static final SaveRepository NO_SAVES = new SaveRepository() {
        @Override
        public void save(SaveData data) {
        }

        @Override
        public Optional<SaveData> load() {
            return Optional.empty();
        }
    };

    @Test
    void classesProgressAtSimilarPace() {
        Map<String, Result> results = new LinkedHashMap<>();
        for (HeroClass heroClass : HeroClasses.ALL) {
            results.put(heroClass.name(), play(heroClass));
        }
        results.forEach((name, r) -> System.out.printf("%-10s stage %2d  Lv %2d  sconfitte %2d  turni/battaglia %.1f%n",
                name, r.stage(), r.level(), r.defeats(), (double) r.turns() / BATTLES));

        int best = results.values().stream().mapToInt(Result::stage).max().orElseThrow();
        int worst = results.values().stream().mapToInt(Result::stage).min().orElseThrow();
        assertTrue(best - worst <= 3, "classi sbilanciate: " + results);
        results.forEach((name, r) -> {
            assertTrue(r.stage() >= 20, name + " si blocca troppo presto: " + r);
            assertTrue(r.defeats() >= 3, name + " non perde mai, troppo facile: " + r);
            assertTrue(r.defeats() <= BATTLES / 3, name + " perde troppo spesso: " + r);
        });
    }

    private static Result play(HeroClass heroClass) {
        Game game = new Game(new StageEnemyFactory(), NO_SAVES, new Random(42));
        game.newGame("Sim", heroClass);
        int defeats = 0;
        int turns = 0;
        int springs = 0;
        for (int i = 0; i < BATTLES; i++) {
            Set<String> usedBuffs = new HashSet<>();
            int battleTurns = 0;
            while (!game.getBattle().isOver()) {
                game.playTurn(choose(game.getHero(), game.getBattle().getEnemy(), usedBuffs));
                turns++;
                assertTrue(++battleTurns < 300, "battaglia infinita con " + heroClass.name());
            }
            boolean won = game.getBattle().heroWon();
            if (!won) {
                defeats++;
                if (DEBUG) {
                    System.out.print(" x" + game.getStage() + "(Lv" + game.getHero().getLevel() + ")");
                }
            }
            while (game.getHero().getInventory().getPotions() < 3 && game.buyPotion()) {
                // tiene sempre qualche pozione di scorta
            }
            if (!won) {
                game.startBattle();
            } else if (!game.canChooseRoute()) {
                game.takeRoute(Route.NORMAL);
            } else if (ppLeftPercent(game.getHero()) < 30 && game.availableRoutes().contains(Route.SPRING)) {
                game.takeRoute(Route.SPRING); // la fonte non conta come battaglia
                springs++;
                game.takeRoute(Route.NORMAL);
            } else {
                game.takeRoute(Route.NORMAL);
            }
        }
        if (DEBUG) {
            System.out.println(" <- " + heroClass.name() + " (fonti: " + springs + ")");
        }
        return new Result(game.getStage(), game.getHero().getLevel(), defeats, turns);
    }

    /** Cura se in pericolo, usa ogni potenziamento una volta, poi la mossa con più danno atteso per turno. */
    private static Action choose(Hero hero, Enemy enemy, Set<String> usedBuffs) {
        int hpPercent = hero.getHp() * 100 / hero.getMaxHp();
        for (Skill skill : hero.getSkills()) {
            if (skill.category().equals("CURA") && hpPercent < 45 && hero.canUse(skill)) {
                return skill;
            }
        }
        DrinkPotion potion = new DrinkPotion(hero.getInventory());
        if (hpPercent < 30 && hero.canUse(potion)) {
            return potion;
        }
        for (Skill skill : hero.getSkills()) {
            boolean worthIt = !skill.targetsEnemy() || (enemy.getStatus() == null && !enemy.isBoss());
            if (skill instanceof StatusSkill && skill.category().equals("STATO") && worthIt
                    && hero.canUse(skill) && usedBuffs.add(skill.name())) {
                return skill;
            }
        }
        Action best = new Attack();
        double bestDamage = expected(hero, enemy, 100, 1, false, Element.NEUTRO);
        for (Skill skill : hero.getSkills()) {
            if (skill instanceof DamageSkill d && hero.canUse(d)) {
                double dmg = expected(hero, enemy, d.powerPercent(), d.hits(), d.piercing(), d.element());
                if (dmg > bestDamage) {
                    best = d;
                    bestDamage = dmg;
                }
            }
        }
        return best;
    }

    private static double expected(Hero hero, Enemy enemy, int power, int hits, boolean piercing, Element element) {
        double defense = piercing ? 0 : enemy.getDefense();
        double effectiveness = element.effectivenessAgainst(enemy.getElement()) / 100.0;
        return hero.getAttack() * power / 100.0 * hits * effectiveness
                * Combatant.DEFENSE_SCALE / (Combatant.DEFENSE_SCALE + defense);
    }

    private static int ppLeftPercent(Hero hero) {
        int left = 0;
        int max = 0;
        for (Skill skill : hero.getSkills()) {
            left += hero.getPp(skill);
            max += skill.maxPp();
        }
        return max == 0 ? 100 : left * 100 / max;
    }
}
