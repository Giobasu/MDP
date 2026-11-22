package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.DamageMove;
import it.unicam.cs.mpgc.rpg132007.action.DrinkPotion;
import it.unicam.cs.mpgc.rpg132007.action.Effect;
import it.unicam.cs.mpgc.rpg132007.action.MoveContext;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.action.StatusMove;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Damaged;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatChanged;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Reward;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import it.unicam.cs.mpgc.rpg132007.model.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class BattleTest {

    /** Classe fissa per i test, così ritoccare il bilanciamento delle classi vere non rompe i conti qui. */
    private static final HeroClass TEST_CLASS = new HeroClass("test", "Test", "", new Appearance(List.of(), "#fff"),
            new Stats(66, 8, 7), new Stats(10, 3, 1), new HeroClasses().byId("warrior").orElseThrow().skills());

    private static final Narrator NARRATOR = new ItalianNarrator();
    /** Tira sempre 99 su 100: mai critici, mai turni saltati per paralisi. Così i conti dei danni sono esatti. */
    private static final RandomGenerator NO_LUCK = () -> 99L << 33;

    private static Battle battle(Hero hero, Enemy enemy) {
        return new Battle(hero, enemy, NO_LUCK, NARRATOR);
    }

    /** Mossa di danno senza descrizione né effetto. */
    private static DamageMove hit(String name, int powerPercent, int hits, boolean piercing) {
        return new DamageMove(name, "", powerPercent, hits, piercing, Effect.NONE);
    }

    private static Hero warrior() {
        return new Hero("Test", TEST_CLASS); // Lv1: 76 HP, 11 ATK, 8 DEF
    }

    private static Enemy enemy(int hp, int attack, int defense) {
        return enemy(hp, attack, defense, false);
    }

    private static Enemy enemy(int hp, int attack, int defense, boolean boss) {
        return new Enemy("Dummy", 1, new Stats(hp, attack, defense), new Appearance(List.of(), "#fff"),
                new Reward(10, 5), new Attack(), 1, boss); // speciale = attacco: attacca sempre
    }

    private static <T extends BattleEvent> T find(List<BattleEvent> events, Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }

    @Test
    void attackDamagesAndEnemyStrikesBack() {
        Hero hero = warrior();
        Enemy enemy = enemy(100, 10, 0);

        battle(hero, enemy).playTurn(new Attack());

        assertEquals(100 - 11, enemy.getHp());
        assertEquals(76 - 10 * 40 / 48, hero.getHp()); // difesa 8: danno x 40/(40+8)
    }

    @Test
    void defenseNeverZeroesDamage() {
        Enemy wall = enemy(100, 0, 1000);
        assertEquals(1, wall.takeDamage(5, false));
        assertEquals(50, enemy(100, 0, Combatant.DEFENSE_SCALE).takeDamage(100, false), "difesa = scala dimezza");
    }

    @Test
    void skillsSpendPpAndAreRejectedWhenEmpty() {
        Hero hero = warrior();
        Skill skill = hero.getSkills().getFirst();
        int maxPp = skill.maxPp().getAsInt();
        Battle battle = battle(hero, enemy(100_000, 0, 0));

        for (int i = 0; i < maxPp; i++) {
            battle.playTurn(skill);
        }
        assertEquals(0, hero.getPp(skill));
        int hp = battle.getEnemy().getHp();
        List<Message> log = battle.playTurn(skill);

        assertEquals(hp, battle.getEnemy().getHp());
        assertEquals(1, log.size());
        assertTrue(hero.canUse(new Attack()), "l'attacco base non ha PP");
        assertTrue(new Attack().maxPp().isEmpty());

        hero.restore();
        assertEquals(0, hero.getPp(skill), "i PP non si ricaricano a inizio battaglia");
        hero.restorePp();
        assertEquals(maxPp, hero.getPp(skill));
    }

    @Test
    void killingTheEnemyEndsTheBattle() {
        Battle battle = battle(warrior(), enemy(5, 10, 0));

        List<Message> log = battle.playTurn(new Attack());

        assertTrue(battle.heroWon());
        assertTrue(log.stream().noneMatch(m -> m.tone() == Message.Tone.ENEMY), "un nemico morto non contrattacca");
    }

    @Test
    void messagesCarryHpAfterEachStep() {
        Hero hero = warrior();
        Enemy enemy = enemy(100, 10, 0);

        List<Message> log = battle(hero, enemy).playTurn(new Attack());

        Message heroLine = log.getFirst();
        assertEquals(100 - 11, heroLine.enemyHp());
        assertEquals(76, heroLine.heroHp(), "il nemico non ha ancora colpito");
        assertEquals(hero.getHp(), log.getLast().heroHp());
    }

    @Test
    void piercingIgnoresDefenseAndMultiHitStopsOnDeath() {
        Hero hero = warrior();
        Enemy armored = enemy(100, 0, 8);
        hit("Magia", 100, 1, true).execute(hero, armored, MoveContext.NORMAL);
        assertEquals(100 - 11, armored.getHp());

        Enemy weak = enemy(15, 0, 0);
        List<BattleEvent> events = hit("Raffica", 100, 4, false).execute(hero, weak, MoveContext.NORMAL);
        assertFalse(weak.isAlive());
        assertEquals(2, find(events, Damaged.class).hits());
    }

    @Test
    void statStagesChangeDamageAndAreClamped() {
        Hero hero = warrior();
        StatusMove rage = new StatusMove("Furia", "STATO", false, "", Effect.raise(Stat.ATTACK, 2));

        rage.execute(hero, enemy(100, 0, 0), MoveContext.NORMAL);
        assertEquals(11 * 6 / 4, hero.getAttack());

        while (hero.getStage(Stat.ATTACK) < Combatant.MAX_STAGE) {
            rage.execute(hero, enemy(100, 0, 0), MoveContext.NORMAL);
        }
        assertEquals(Combatant.MAX_STAGE, hero.getStage(Stat.ATTACK), "non supera il massimo");
        StatChanged maxed = find(rage.execute(hero, enemy(100, 0, 0), MoveContext.NORMAL), StatChanged.class);
        assertEquals(0, maxed.changed(), "già al massimo");

        hero.restore();
        assertEquals(0, hero.getStage(Stat.ATTACK), "i modificatori durano una sola battaglia");

        for (int i = 0; i <= Combatant.MAX_STAGE; i++) {
            hero.changeStage(Stat.DEFENSE, -1);
        }
        assertEquals(-Combatant.MAX_STAGE, hero.getStage(Stat.DEFENSE), "non scende sotto il minimo");
    }

    @Test
    void poisonHurtsAtEndOfTurnThenWearsOff() {
        Hero hero = warrior();
        Enemy enemy = enemy(200, 0, 0);
        Battle battle = battle(hero, enemy);
        new DamageMove("Veleno", "", 0, 1, true, Effect.inflict(Status.VELENO, 2)).execute(hero, enemy, MoveContext.NORMAL);
        int afterHit = enemy.getHp();

        Action wait = new StatusMove("Attesa", "STATO", false, "", Effect.NONE);
        battle.playTurn(wait);
        battle.playTurn(wait);
        battle.playTurn(wait);

        int tick = 200 * Status.VELENO.damagePercent() / 100;
        assertEquals(afterHit - 2 * tick, enemy.getHp());
        assertNull(enemy.getStatus());
    }

    @Test
    void drainHealsTheUser() {
        Hero hero = warrior();
        hero.takeDamage(40, false);
        int before = hero.getHp();

        new DamageMove("Vampiro", "", 100, 1, true, Effect.drain(50)).execute(hero, enemy(100, 0, 0), MoveContext.NORMAL);

        assertEquals(before + 11 / 2, hero.getHp());
    }

    @Test
    void potionHealsAndIsConsumed() {
        Hero hero = warrior();
        hero.takeDamage(60, false);
        int before = hero.getHp();
        int potions = hero.getInventory().getPotions();

        battle(hero, enemy(100, 0, 0)).playTurn(hero.getItems().getFirst());

        assertEquals(potions - 1, hero.getInventory().getPotions());
        assertEquals(before + 38 - 1, hero.getHp()); // +50% di 76, poi il nemico fa il danno minimo di 1
    }

    @Test
    void potionIsRejectedWhenNoneLeftOrHpFull() {
        Hero hero = warrior();
        DrinkPotion potion = new DrinkPotion(hero.getInventory());
        assertFalse(hero.canUse(potion), "HP già al massimo");
        hero.takeDamage(10, false);
        assertTrue(hero.canUse(potion));
        while (hero.getInventory().usePotion()) {
            // svuota lo zaino
        }
        assertFalse(hero.canUse(potion));
        assertThrows(IllegalStateException.class, () -> potion.execute(hero, hero, MoveContext.NORMAL));
    }

    @Test
    void sleepSkipsTurnsAndBossesAreImmune() {
        Hero hero = warrior();
        Enemy enemy = enemy(1000, 10, 0);
        assertTrue(enemy.inflict(Status.SONNO, 2));
        Battle battle = battle(hero, enemy);

        battle.playTurn(new Attack());
        battle.playTurn(new Attack());
        assertEquals(hero.getMaxHp(), hero.getHp(), "dorme per due turni");
        battle.playTurn(new Attack());
        assertTrue(hero.getHp() < hero.getMaxHp(), "al terzo turno si sveglia e attacca");
        assertNull(enemy.getStatus());

        Enemy boss = enemy(1000, 10, 0, true);
        assertFalse(boss.inflict(Status.SONNO, 2));
        assertTrue(boss.inflict(Status.VELENO, 2), "i boss resistono solo al sonno");
        assertFalse(boss.inflict(Status.PARALISI, 2), "uno stato alla volta");
    }

    @Test
    void paralysisUsesTheRandomGenerator() {
        Hero hero = warrior();
        Enemy enemy = enemy(1000, 10, 0);
        enemy.inflict(Status.PARALISI, 5);

        new Battle(hero, enemy, () -> 0L, NARRATOR).playTurn(new Attack()); // 0 -> tira sempre "salta il turno"
        assertEquals(hero.getMaxHp(), hero.getHp());

        new Battle(hero, enemy, NO_LUCK, NARRATOR).playTurn(new Attack()); // tira 99 -> agisce
        assertTrue(hero.getHp() < hero.getMaxHp());
    }

    @Test
    void sleepNeverUsesTheRandomGenerator() {
        assertTrue(Status.SONNO.skipsTurn(() -> {
            throw new AssertionError("il sonno non tira dadi");
        }));
        assertFalse(Status.VELENO.skipsTurn(() -> {
            throw new AssertionError("il veleno non tira dadi");
        }));
    }

    @Test
    void burnLowersAttackAndHurts() {
        Hero hero = warrior();
        int attack = hero.getAttack();
        hero.inflict(Status.BRUCIATURA, 3);
        assertEquals(attack * 3 / 4, hero.getAttack());

        battle(hero, enemy(1000, 0, 0)).playTurn(new Attack());
        assertEquals(hero.getMaxHp() - hero.getMaxHp() * Status.BRUCIATURA.damagePercent() / 100 - 1, hero.getHp());
    }

    @Test
    void movesCarryAnimationInfo() {
        Hero hero = warrior();
        List<Message> log = battle(hero, enemy(1000, 10, 0)).playTurn(new Attack());

        Message.Fx heroFx = log.getFirst().fx();
        assertTrue(heroFx.onEnemySide() && heroFx.fromHero() && !heroFx.support());
        Message enemyLine = log.stream().filter(m -> m.tone() == Message.Tone.ENEMY && m.fx() != null).findFirst().orElseThrow();
        assertFalse(enemyLine.fx().onEnemySide(), "il colpo del nemico si anima sul lato dell'eroe");
    }

    @Test
    void battleUsesTheGivenNarrator() {
        Battle battle = new Battle(warrior(), enemy(1000, 0, 0), () -> 0L, event -> "#");

        List<Message> log = battle.playTurn(new Attack());

        assertTrue(log.stream().allMatch(m -> m.text().equals("#")), "nessun testo scritto fuori dal narratore");
    }

    @Test
    void enemyUsesItsSpecialEveryNTurns() {
        DamageMove special = hit("Speciale", 200, 1, false);
        Enemy enemy = new Enemy("Dummy", 1, new Stats(10, 1, 0), new Appearance(List.of(), "#fff"),
                new Reward(10, 5), special, 3, false);

        assertInstanceOf(Attack.class, enemy.chooseAction());
        assertInstanceOf(Attack.class, enemy.chooseAction());
        assertSame(special, enemy.chooseAction());
        assertInstanceOf(Attack.class, enemy.chooseAction());
    }

    @Test
    void criticalHitsDoMoreDamageAndAreAnnounced() {
        Enemy enemy = enemy(1000, 0, 0);

        List<Message> log = new Battle(warrior(), enemy, () -> 0L, NARRATOR).playTurn(new Attack()); // 0 -> critico

        assertEquals(1000 - 11 * MoveContext.CRITICAL_POWER_PERCENT / 100, enemy.getHp());
        assertTrue(log.stream().anyMatch(m -> m.text().startsWith("Colpo critico!")));
    }

    @Test
    void criticalComesOnlyFromTheContext() {
        DamageMove move = hit("Colpo", 100, 2, true);
        Damaged normal = find(move.execute(warrior(), enemy(1000, 0, 0), MoveContext.NORMAL), Damaged.class);
        Damaged critical = find(move.execute(warrior(), enemy(1000, 0, 0), new MoveContext(true)), Damaged.class);

        assertFalse(normal.critical());
        assertTrue(critical.critical());
        assertEquals(2 * (11 * MoveContext.CRITICAL_POWER_PERCENT / 100), critical.amount(), "il critico vale per ogni colpo");
    }
}
