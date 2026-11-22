package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClassCatalog;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.narration.ItalianNarrator;
import it.unicam.cs.mpgc.rpg132007.model.Reward;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import it.unicam.cs.mpgc.rpg132007.persistence.JsonSaveRepository;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveListener;
import it.unicam.cs.mpgc.rpg132007.world.EnemyFactory;
import it.unicam.cs.mpgc.rpg132007.world.StageEnemyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    /** EXP da Lv1 a Lv7 (20 + 40 + ... + 120): al Lv7 si sblocca la quinta skill. */
    private static final int EXP_TO_LV7 = 420;

    private static final HeroClassCatalog CLASSES = new HeroClasses();

    @TempDir
    Path dir;

    private Game game(EnemyFactory enemies, String saveFile) {
        return new Game(enemies, CLASSES, new JsonSaveRepository(dir.resolve(saveFile)), new ItalianNarrator(), new Random(1));
    }

    private Game newGame() {
        Game game = game(new StageEnemyFactory(), "save.json");
        game.newGame("Test", CLASSES.byId("lancer").orElseThrow());
        return game;
    }

    /** Il salvataggio automatico è in background: i test devono aspettarlo prima che JUnit cancelli la cartella. */
    private static CountDownLatch awaitAutoSave(Game game) {
        CountDownLatch done = new CountDownLatch(1);
        game.setAutoSaveListener(new SaveListener() {
            @Override
            public void saved() {
                done.countDown();
            }

            @Override
            public void failed(IOException error) {
                // il latch non scende: il test che aspetta fallisce per timeout
            }
        });
        return done;
    }

    private static int maxPp(Skill skill) {
        return skill.maxPp().getAsInt();
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
    void ppComeBackAfterBossOrDefeatButNotAfterNormalWins() throws InterruptedException {
        assertPpRestored(false, false, false);
        assertPpRestored(true, false, true);
        assertPpRestored(false, true, true);
    }

    /** Spende un PP, gioca una battaglia contro un nemico finto (boss o normale, debole o fortissimo) e controlla i PP. */
    private void assertPpRestored(boolean boss, boolean heroLoses, boolean expectRestored) throws InterruptedException {
        EnemyFactory factory = stage -> new Enemy("Finto", stage, new Stats(heroLoses ? 10_000 : 1, heroLoses ? 10_000 : 0, 0),
                new Appearance(List.of(), "#fff"), new Reward(1, 1), new Attack(), 1, boss);
        Game game = game(factory, "pp.json");
        game.newGame("Test", CLASSES.byId("lancer").orElseThrow());
        CountDownLatch autoSave = awaitAutoSave(game);
        Skill skill = game.getHero().getSkills().getFirst();
        game.getHero().spendPp(skill);

        game.playTurn(new Attack());

        if (boss && !heroLoses) {
            assertTrue(autoSave.await(5, TimeUnit.SECONDS), "il salvataggio automatico deve finire");
        }
        assertTrue(game.getBattle().isOver());
        assertEquals(expectRestored ? maxPp(skill) : maxPp(skill) - 1, game.getHero().getPp(skill),
                "boss=" + boss + " sconfitta=" + heroLoses);
    }

    @Test
    void springRestoresPpAndSkipsAStageButNotTwiceInARow() {
        Game game = newGame();
        Skill skill = game.getHero().getSkills().getFirst();
        game.getHero().spendPp(skill);

        assertTrue(game.availableRoutes().contains(Route.SPRING));
        assertFalse(game.takeRoute(Route.SPRING).isEmpty(), "la fonte racconta cosa è successo");

        assertEquals(maxPp(skill), game.getHero().getPp(skill));
        assertEquals(2, game.getStage());
        assertFalse(game.availableRoutes().contains(Route.SPRING));
        assertThrows(IllegalStateException.class, () -> game.takeRoute(Route.SPRING));
        assertTrue(game.takeRoute(Route.NORMAL).isEmpty(), "un sentiero con battaglia non ha messaggi");
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

        Game loaded = game(new StageEnemyFactory(), "save.json");
        loaded.load();
        assertEquals(maxPp(skill) - 2, loaded.getHero().getPp(skill));
    }

    @Test
    void learnedSkillsAreSavedAndLoaded() throws IOException {
        Game game = newGame();
        game.getHero().gainExp(EXP_TO_LV7);
        Skill forgotten = game.getHero().getSkills().getFirst();
        List<Message> log = game.learnPendingSkill(forgotten);
        assertFalse(log.isEmpty());
        game.save();

        Game loaded = game(new StageEnemyFactory(), "save.json");
        loaded.load();
        assertEquals(game.getHero().getSkillNames(), loaded.getHero().getSkillNames());
        assertFalse(loaded.getHero().knows(forgotten));
    }

    @Test
    void bossVictoryAutosavesInBackground() throws InterruptedException {
        EnemyFactory weakBoss = stage -> new Enemy("Boss", stage, new Stats(1, 0, 0),
                new Appearance(List.of(), "#fff"), new Reward(1, 1), new Attack(), 1, true);
        Game game = game(weakBoss, "auto.json");
        game.newGame("Test", CLASSES.byId("lancer").orElseThrow());
        CountDownLatch done = awaitAutoSave(game);

        game.playTurn(new Attack());

        assertTrue(done.await(5, TimeUnit.SECONDS), "il salvataggio automatico deve finire");
        assertTrue(Files.exists(dir.resolve("auto.json")));
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

        Game loaded = game(new StageEnemyFactory(), "save.json");
        assertTrue(loaded.load());
        assertEquals(game.getHero().getLevel(), loaded.getHero().getLevel());
        assertEquals("lancer", loaded.getHero().getHeroClass().id());
        assertEquals(42, loaded.getHero().getInventory().getGold());
    }

    @Test
    void unknownHeroClassInSaveIsReported() throws IOException {
        Files.writeString(dir.resolve("save.json"), """
                { "name": "X", "heroClass": "paladin", "level": 1, "exp": 0, "stage": 1,
                  "gold": 0, "potions": 0, "usedPp": {}, "skills": [] }
                """);
        Game game = game(new StageEnemyFactory(), "save.json");
        assertThrows(IOException.class, game::load);
    }

    @Test
    void heroClassesComeFromTheInjectedCatalog() throws IOException {
        Game game = newGame();
        game.save();
        HeroClassCatalog empty = List::of;
        Game other = new Game(new StageEnemyFactory(), empty, new JsonSaveRepository(dir.resolve("save.json")),
                new ItalianNarrator(), new Random(1));
        assertThrows(IOException.class, other::load, "con un catalogo vuoto la classe salvata non esiste");
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
