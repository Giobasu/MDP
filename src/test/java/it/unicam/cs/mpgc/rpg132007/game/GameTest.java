package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.battle.AggressiveAI;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Reward;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import it.unicam.cs.mpgc.rpg132007.world.EnemyFactory;
import it.unicam.cs.mpgc.rpg132007.persistence.PropertiesSaveRepository;
import it.unicam.cs.mpgc.rpg132007.world.StageEnemyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @TempDir
    Path dir;

    private Game newGame() {
        Game game = new Game(new StageEnemyFactory(), new PropertiesSaveRepository(dir.resolve("save.properties")));
        game.newGame("Test", HeroClasses.byId("lancer").orElseThrow());
        return game;
    }

    @Test
    void winningGivesGoldAndAdvancesStage() {
        Game game = newGame();
        while (!game.getBattle().isOver()) {
            game.playTurn(new Attack());
        }
        assertTrue(game.getBattle().heroWon(), "lo stage 1 deve essere battibile solo attaccando");
        assertEquals(2, game.getStage());
        assertTrue(game.getHero().getInventory().getGold() > 0);
    }

    @Test
    void ppComeBackAfterBossOrDefeatButNotAfterNormalWins() {
        assertPpRestored(false, false, false);
        assertPpRestored(true, false, true);
        assertPpRestored(false, true, true);
    }

    /** Spende un PP, gioca una battaglia contro un nemico finto (boss o normale, debole o fortissimo) e controlla i PP. */
    private void assertPpRestored(boolean boss, boolean heroLoses, boolean expectRestored) {
        EnemyFactory factory = stage -> new Enemy("Finto", stage, new Stats(heroLoses ? 10_000 : 1, heroLoses ? 10_000 : 0, 0),
                new Appearance(List.of(), "#fff"), new Reward(1, 1), new AggressiveAI(), boss);
        Game game = new Game(factory, new PropertiesSaveRepository(dir.resolve("pp.properties")));
        game.newGame("Test", HeroClasses.byId("lancer").orElseThrow());
        Skill skill = game.getHero().getSkills().getFirst();
        game.getHero().spendPp(skill);

        game.playTurn(new Attack());

        assertTrue(game.getBattle().isOver());
        assertEquals(expectRestored ? skill.maxPp() : skill.maxPp() - 1, game.getHero().getPp(skill),
                "boss=" + boss + " sconfitta=" + heroLoses);
    }

    @Test
    void springRestoresPpAndSkipsAStageButNotTwiceInARow() {
        Game game = newGame();
        Skill skill = game.getHero().getSkills().getFirst();
        game.getHero().spendPp(skill);

        assertTrue(game.availableRoutes().contains(Route.SPRING));
        game.takeRoute(Route.SPRING);

        assertEquals(skill.maxPp(), game.getHero().getPp(skill));
        assertEquals(2, game.getStage());
        assertFalse(game.availableRoutes().contains(Route.SPRING));
        assertThrows(IllegalStateException.class, () -> game.takeRoute(Route.SPRING));
        game.takeRoute(Route.NORMAL);
        assertTrue(game.availableRoutes().contains(Route.SPRING));
    }

    @Test
    void eliteEnemiesAreStrongerAndPayMore() {
        StageEnemyFactory factory = new StageEnemyFactory();
        Enemy normal = factory.create(3);
        Enemy elite = factory.createElite(3);
        assertTrue(elite.getMaxHp() > normal.getMaxHp());
        assertTrue(elite.getReward().exp() > normal.getReward().exp());
        assertFalse(factory.createElite(5).getName().contains("Élite"), "i boss non hanno versione élite");
    }

    @Test
    void ppAreSavedAndLoaded() throws IOException {
        Game game = newGame();
        Skill skill = game.getHero().getSkills().getFirst();
        game.getHero().spendPp(skill);
        game.getHero().spendPp(skill);
        game.save();

        Game loaded = new Game(new StageEnemyFactory(), new PropertiesSaveRepository(dir.resolve("save.properties")));
        loaded.load();
        assertEquals(skill.maxPp() - 2, loaded.getHero().getPp(skill));
    }

    @Test
    void bossVictoryAutosaves() {
        EnemyFactory weakBoss = stage -> new Enemy("Boss", stage, new Stats(1, 0, 0),
                new Appearance(List.of(), "#fff"), new Reward(1, 1), new AggressiveAI(), true);
        Path file = dir.resolve("auto.properties");
        Game game = new Game(weakBoss, new PropertiesSaveRepository(file));
        game.newGame("Test", HeroClasses.byId("lancer").orElseThrow());

        game.playTurn(new Attack());

        assertTrue(Files.exists(file));
    }

    @Test
    void buyPotionNeedsGold() {
        Game game = newGame();
        assertFalse(game.buyPotion());
        game.getHero().getInventory().addGold(Game.POTION_PRICE);
        int potions = game.getHero().getInventory().getPotions();
        assertTrue(game.buyPotion());
        assertEquals(potions + 1, game.getHero().getInventory().getPotions());
    }

    @Test
    void saveAndLoadRestoreTheHero() throws IOException {
        Game game = newGame();
        game.getHero().gainExp(100);
        game.getHero().getInventory().addGold(42);
        game.save();

        Game loaded = new Game(new StageEnemyFactory(), new PropertiesSaveRepository(dir.resolve("save.properties")));
        assertTrue(loaded.load());
        assertEquals(game.getHero().getLevel(), loaded.getHero().getLevel());
        assertEquals("lancer", loaded.getHero().getHeroClass().id());
        assertEquals(42, loaded.getHero().getInventory().getGold());
    }

    @Test
    void unknownHeroClassInSaveIsReported() throws IOException {
        Files.writeString(dir.resolve("save.properties"),
                "name=X\nheroClass=paladin\nlevel=1\nexp=0\nstage=1\ngold=0\npotions=0\n");
        Game game = new Game(new StageEnemyFactory(), new PropertiesSaveRepository(dir.resolve("save.properties")));
        assertThrows(IOException.class, game::load);
    }

    @Test
    void everyFifthStageIsABoss() {
        StageEnemyFactory factory = new StageEnemyFactory();
        for (int stage = 1; stage <= 40; stage++) {
            Enemy enemy = factory.create(stage);
            assertEquals(stage % StageEnemyFactory.BOSS_EVERY == 0, enemy.isBoss(), "stage " + stage);
        }
    }
}
