package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.DamageSkill;
import it.unicam.cs.mpgc.rpg132007.action.Defend;
import it.unicam.cs.mpgc.rpg132007.action.DrinkPotion;
import it.unicam.cs.mpgc.rpg132007.action.Effect;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.action.StatusSkill;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Element;
import it.unicam.cs.mpgc.rpg132007.model.Status;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Hero;
import it.unicam.cs.mpgc.rpg132007.model.Reward;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BattleTest {

    /** Classe fissa per i test, così ritoccare il bilanciamento delle classi vere non rompe i conti qui. */
    private static final HeroClass TEST_CLASS = new HeroClass("test", "Test", "", new Appearance(List.of(), "#fff"),
            new Stats(66, 8, 7), new Stats(10, 3, 1), HeroClasses.byId("warrior").orElseThrow().skills());

    private static Hero warrior() {
        return new Hero("Test", TEST_CLASS); // Lv1: 76 HP, 11 ATK, 8 DEF
    }

    private static Enemy enemy(int hp, int attack, int defense) {
        return new Enemy("Dummy", 1, new Stats(hp, attack, defense), new Appearance(List.of(), "#fff"),
                new Reward(10, 5), new AggressiveAI(), false);
    }

    @Test
    void attackDamagesAndEnemyStrikesBack() {
        Hero hero = warrior();
        Enemy enemy = enemy(100, 10, 0);

        new Battle(hero, enemy).playTurn(new Attack());

        assertEquals(100 - 11, enemy.getHp());
        assertEquals(76 - 10 * 40 / 48, hero.getHp()); // difesa 8: danno x 40/(40+8)
    }

    @Test
    void defendHalvesDamage() {
        Hero hero = warrior();

        new Battle(hero, enemy(100, 10, 0)).playTurn(new Defend());

        assertEquals(76 - 10 * 40 / 48 / 2, hero.getHp());
    }

    @Test
    void defenseNeverZeroesDamage() {
        Enemy wall = enemy(100, 0, 1000);
        assertEquals(1, wall.takeDamage(5));
        assertEquals(50, enemy(100, 0, Combatant.DEFENSE_SCALE).takeDamage(100), "difesa = scala dimezza");
    }

    @Test
    void skillsSpendPpAndAreRejectedWhenEmpty() {
        Hero hero = warrior();
        Skill skill = hero.getSkills().getFirst();
        Battle battle = new Battle(hero, enemy(100_000, 0, 0));

        for (int i = 0; i < skill.maxPp(); i++) {
            battle.playTurn(skill);
        }
        assertEquals(0, hero.getPp(skill));
        int hp = battle.getEnemy().getHp();
        List<Message> log = battle.playTurn(skill);

        assertEquals(hp, battle.getEnemy().getHp());
        assertEquals(1, log.size());
        assertTrue(hero.canUse(new Attack()), "l'attacco base non ha PP");

        hero.restore();
        assertEquals(0, hero.getPp(skill), "i PP non si ricaricano a inizio battaglia");
        hero.restorePp();
        assertEquals(skill.maxPp(), hero.getPp(skill));
    }

    @Test
    void killingTheEnemyEndsTheBattle() {
        Battle battle = new Battle(warrior(), enemy(5, 10, 0));

        List<Message> log = battle.playTurn(new Attack());

        assertTrue(battle.heroWon());
        assertTrue(log.stream().noneMatch(m -> m.tone() == Message.Tone.ENEMY), "un nemico morto non contrattacca");
    }

    @Test
    void messagesCarryHpAfterEachStep() {
        Hero hero = warrior();
        Enemy enemy = enemy(100, 10, 0);

        List<Message> log = new Battle(hero, enemy).playTurn(new Attack());

        Message heroLine = log.getFirst();
        assertEquals(100 - 11, heroLine.enemyHp());
        assertEquals(76, heroLine.heroHp(), "il nemico non ha ancora colpito");
        assertEquals(hero.getHp(), log.getLast().heroHp());
    }

    @Test
    void piercingIgnoresDefenseAndMultiHitStopsOnDeath() {
        Hero hero = warrior();
        Enemy armored = enemy(100, 0, 8);
        new DamageSkill("Magia", 0, 0, 100, 1, true).execute(hero, armored);
        assertEquals(100 - 11, armored.getHp());

        Enemy weak = enemy(15, 0, 0);
        String line = new DamageSkill("Raffica", 0, 0, 100, 4, false).execute(hero, weak);
        assertFalse(weak.isAlive());
        assertTrue(line.contains("2 colpi"), line);
    }

    @Test
    void statStagesChangeDamageAndAreClamped() {
        Hero hero = warrior();
        StatusSkill rage = new StatusSkill("Furia", 0, 0, Element.NEUTRO, "STATO", false, "", Effect.raise(Stat.ATTACK, 2));

        rage.execute(hero, enemy(100, 0, 0));
        assertEquals(11 * 6 / 4, hero.getAttack());

        rage.execute(hero, enemy(100, 0, 0));
        assertEquals(Combatant.MAX_STAGE, hero.getStage(Stat.ATTACK));
        assertTrue(rage.execute(hero, enemy(100, 0, 0)).contains("non può salire"));

        hero.restore();
        assertEquals(0, hero.getStage(Stat.ATTACK), "i modificatori durano una sola battaglia");
    }

    @Test
    void poisonHurtsAtEndOfTurnThenWearsOff() {
        Hero hero = warrior();
        Enemy enemy = enemy(200, 0, 0);
        Battle battle = new Battle(hero, enemy);
        new DamageSkill("Veleno", 0, 0, Element.NEUTRO, 0, 1, true, "", Effect.poison(2)).execute(hero, enemy);
        int afterHit = enemy.getHp();

        battle.playTurn(new Defend());
        battle.playTurn(new Defend());
        battle.playTurn(new Defend());

        int tick = 200 * Status.VELENO.damagePercent() / 100;
        assertEquals(afterHit - 2 * tick, enemy.getHp());
        assertNull(enemy.getStatus());
    }

    @Test
    void drainHealsTheUser() {
        Hero hero = warrior();
        hero.takeDamage(40);
        int before = hero.getHp();

        new DamageSkill("Vampiro", 0, 0, Element.NEUTRO, 100, 1, true, "", Effect.drain(50)).execute(hero, enemy(100, 0, 0));

        assertEquals(before + 11 / 2, hero.getHp());
    }

    @Test
    void potionHealsAndIsConsumed() {
        Hero hero = warrior();
        hero.takeDamage(60);
        int before = hero.getHp();
        int potions = hero.getInventory().getPotions();

        new Battle(hero, enemy(100, 0, 0)).playTurn(new DrinkPotion(hero.getInventory()));

        assertEquals(potions - 1, hero.getInventory().getPotions());
        assertEquals(before + 38 - 1, hero.getHp()); // +50% di 76, poi il nemico fa il danno minimo di 1
    }

    private static Enemy enemy(int hp, int attack, int defense, Element element, boolean boss) {
        return new Enemy("Dummy", 1, new Stats(hp, attack, defense), new Appearance(List.of(), "#fff"),
                new Reward(10, 5), new AggressiveAI(), boss, element);
    }

    @Test
    void elementsChangeDamage() {
        Hero hero = warrior(); // ATK 11
        DamageSkill fire = new DamageSkill("Fuoco", 0, 0, Element.FUOCO, 100, 1, true, "", Effect.NONE);

        Enemy plant = enemy(100, 0, 0, Element.NATURA, false);
        assertTrue(fire.execute(hero, plant).contains("superefficace"));
        assertEquals(100 - 11 * 150 / 100, plant.getHp());

        Enemy water = enemy(100, 0, 0, Element.ACQUA, false);
        assertTrue(fire.execute(hero, water).contains("Non è molto efficace"));
        assertEquals(100 - 11 * 60 / 100, water.getHp());

        assertEquals(Element.NORMAL, Element.FUOCO.effectivenessAgainst(Element.LUCE));
        assertEquals(Element.SUPER_EFFECTIVE, Element.LUCE.effectivenessAgainst(Element.OMBRA));
        assertEquals(Element.SUPER_EFFECTIVE, Element.OMBRA.effectivenessAgainst(Element.LUCE));
        assertEquals(Element.NORMAL, Element.NEUTRO.effectivenessAgainst(Element.FUOCO));
    }

    @Test
    void sleepSkipsTurnsAndBossesAreImmune() {
        Hero hero = warrior();
        Enemy enemy = enemy(1000, 10, 0);
        assertTrue(enemy.inflict(Status.SONNO, 2));
        Battle battle = new Battle(hero, enemy);

        battle.playTurn(new Attack());
        battle.playTurn(new Attack());
        assertEquals(hero.getMaxHp(), hero.getHp(), "dorme per due turni");
        battle.playTurn(new Attack());
        assertTrue(hero.getHp() < hero.getMaxHp(), "al terzo turno si sveglia e attacca");
        assertNull(enemy.getStatus());

        Enemy boss = enemy(1000, 10, 0, Element.NEUTRO, true);
        assertFalse(boss.inflict(Status.SONNO, 2));
        assertTrue(boss.inflict(Status.VELENO, 2), "i boss resistono solo al sonno");
        assertFalse(boss.inflict(Status.PARALISI, 2), "uno stato alla volta");
    }

    @Test
    void paralysisUsesTheRandomGenerator() {
        Hero hero = warrior();
        Enemy enemy = enemy(1000, 10, 0);
        enemy.inflict(Status.PARALISI, 5);

        new Battle(hero, enemy, () -> 0L).playTurn(new Attack()); // 0 -> tira sempre "salta il turno"
        assertEquals(hero.getMaxHp(), hero.getHp());

        new Battle(hero, enemy, () -> 99L << 33).playTurn(new Attack()); // tira 99 -> agisce
        assertTrue(hero.getHp() < hero.getMaxHp());
    }

    @Test
    void burnLowersAttackAndHurts() {
        Hero hero = warrior();
        int attack = hero.getAttack();
        hero.inflict(Status.BRUCIATURA, 3);
        assertEquals(attack * 3 / 4, hero.getAttack());

        new Battle(hero, enemy(1000, 0, 0)).playTurn(new Attack());
        assertEquals(hero.getMaxHp() - hero.getMaxHp() * Status.BRUCIATURA.damagePercent() / 100 - 1, hero.getHp());
    }

    @Test
    void movesCarryAnimationInfo() {
        Hero hero = warrior();
        List<Message> log = new Battle(hero, enemy(1000, 10, 0)).playTurn(new Attack());

        Message.Fx heroFx = log.getFirst().fx();
        assertTrue(heroFx.onEnemySide() && heroFx.fromHero() && !heroFx.support());
        Message enemyLine = log.stream().filter(m -> m.tone() == Message.Tone.ENEMY && m.fx() != null).findFirst().orElseThrow();
        assertFalse(enemyLine.fx().onEnemySide(), "il colpo del nemico si anima sul lato dell'eroe");
    }

    @Test
    void specialMoveAIUsesSpecialEveryNTurns() {
        DamageSkill special = new DamageSkill("Speciale", 0, 0, 200, 1, false);
        SpecialMoveAI ai = new SpecialMoveAI(special, 3);
        Enemy self = enemy(10, 1, 0);

        assertInstanceOf(Attack.class, ai.chooseAction(self, warrior()));
        assertInstanceOf(Attack.class, ai.chooseAction(self, warrior()));
        assertSame(special, ai.chooseAction(self, warrior()));
    }
}
