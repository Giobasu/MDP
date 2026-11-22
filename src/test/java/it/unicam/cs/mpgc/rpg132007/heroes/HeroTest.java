package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.moves.Attack;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeroTest {

    private static final HeroClassCatalog CLASSES = new HeroClasses();
    private static final HeroClass WARRIOR = CLASSES.byId("warrior").orElseThrow();
    /** EXP da Lv1 a Lv7 (20 + 40 + ... + 120): al Lv7 il guerriero sblocca la quinta skill. */
    private static final int EXP_TO_LV7 = 420;

    @Test
    void levelUpRaisesStatsAndUnlocksSkills() {
        Hero hero = new Hero("Test", WARRIOR);
        assertEquals(1, hero.getMoveset().getSkills().size(), "al Lv1 si ha già la prima abilità");

        LevelUp levelUp = hero.gainExp(20 + 40 + 5); // Lv1 -> Lv3, avanzano 5 EXP

        assertEquals(2, levelUp.levelsGained());
        assertEquals(List.of("Rafforzatore", "Colpo di Scudo"), levelUp.learned().stream().map(Skill::name).toList());
        assertEquals(List.of(), levelUp.waiting());
        assertEquals(3, hero.getLevel());
        assertEquals(5, hero.getExp());
        assertEquals(WARRIOR.statsFor(3).attack(), hero.getAttack());
        assertEquals(3, hero.getMoveset().getSkills().size());
        assertEquals("Colpo di Scudo", hero.getMoveset().getSkills().getLast().name());
    }

    @Test
    void movesAreTheBasicAttackPlusUnlockedSkills() {
        Hero hero = new Hero("Test", WARRIOR);
        assertInstanceOf(Attack.class, hero.getMoves().getFirst());
        assertEquals(1 + hero.getMoveset().getSkills().size(), hero.getMoves().size());
        assertEquals("Pozione  x3", hero.getItems().getFirst().label());
    }

    @Test
    void knowsAtMostFourSkillsAndTheFifthWaits() {
        Hero hero = new Hero("Test", WARRIOR);
        hero.gainExp(EXP_TO_LV7);

        assertEquals(7, hero.getLevel());
        assertEquals(Moveset.MAX_SKILLS, hero.getMoveset().getSkills().size());
        Skill newSkill = hero.getMoveset().getPendingSkill().orElseThrow();
        assertEquals("Grido di Guerra", newSkill.name());
        assertFalse(hero.getMoveset().knows(newSkill));
    }

    @Test
    void learningForgetsTheChosenSkillAndItsPp() {
        Hero hero = new Hero("Test", WARRIOR);
        Skill first = hero.getMoveset().getSkills().getFirst();
        hero.getMoveset().spendPp(first);
        hero.gainExp(EXP_TO_LV7);
        Skill newSkill = hero.getMoveset().getPendingSkill().orElseThrow();

        hero.getMoveset().learnPendingSkill(first);

        assertSame(newSkill, hero.getMoveset().getSkills().getFirst(), "la nuova prende il posto della dimenticata");
        assertFalse(hero.getMoveset().knows(first));
        assertTrue(hero.getMoveset().getPendingSkill().isEmpty());
        assertFalse(hero.getMoveset().getUsedPp().containsKey(first.name()));
        assertThrows(IllegalStateException.class, () -> hero.getMoveset().learnPendingSkill(newSkill), "niente più in attesa");
    }

    @Test
    void skippingKeepsTheOldSkills() {
        Hero hero = new Hero("Test", WARRIOR);
        hero.gainExp(EXP_TO_LV7);
        List<Skill> before = hero.getMoveset().getSkills();

        hero.getMoveset().skipPendingSkill();

        assertEquals(before, hero.getMoveset().getSkills());
        assertTrue(hero.getMoveset().getPendingSkill().isEmpty());
    }

    @Test
    void everySkillHasLimitedPp() {
        CLASSES.all().forEach(c -> c.skills().forEach(s ->
                assertTrue(s.maxPp().isPresent(), c.name() + " / " + s.name() + " deve avere PP")));
    }

    @Test
    void everyClassHasUniqueIdAndValidStats() {
        assertEquals(CLASSES.all().size(), CLASSES.all().stream().map(HeroClass::id).distinct().count());
        CLASSES.all().forEach(c -> assertDoesNotThrow(() -> new Hero("Test", c)));
    }

    @Test
    void inventoryNeverGoesNegative() {
        Inventory inv = new Inventory(10, 0);
        assertFalse(inv.spendGold(11));
        assertFalse(inv.usePotion());
        assertEquals(10, inv.getGold());
    }
}
