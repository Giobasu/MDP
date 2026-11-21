package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.model.Inventory;
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
        assertEquals(1, hero.getSkills().size(), "al Lv1 si ha già la prima abilità");

        int gained = hero.gainExp(20 + 40 + 5); // Lv1 -> Lv3, avanzano 5 EXP

        assertEquals(2, gained);
        assertEquals(3, hero.getLevel());
        assertEquals(5, hero.getExp());
        assertEquals(WARRIOR.statsFor(3).attack(), hero.getAttack());
        assertEquals(3, hero.getSkills().size());
        assertEquals("Colpo di Scudo", hero.getSkills().getLast().name());
    }

    @Test
    void movesAreTheBasicAttackPlusUnlockedSkills() {
        Hero hero = new Hero("Test", WARRIOR);
        assertInstanceOf(Attack.class, hero.getMoves().getFirst());
        assertEquals(1 + hero.getSkills().size(), hero.getMoves().size());
        assertEquals("Pozione  x3", hero.getItems().getFirst().label());
    }

    @Test
    void knowsAtMostFourSkillsAndTheFifthWaits() {
        Hero hero = new Hero("Test", WARRIOR);
        hero.gainExp(EXP_TO_LV7);

        assertEquals(7, hero.getLevel());
        assertEquals(Hero.MAX_SKILLS, hero.getSkills().size());
        Skill newSkill = hero.getPendingSkill().orElseThrow();
        assertEquals("Grido di Guerra", newSkill.name());
        assertFalse(hero.knows(newSkill));
    }

    @Test
    void learningForgetsTheChosenSkillAndItsPp() {
        Hero hero = new Hero("Test", WARRIOR);
        Skill first = hero.getSkills().getFirst();
        hero.spendPp(first);
        hero.gainExp(EXP_TO_LV7);
        Skill newSkill = hero.getPendingSkill().orElseThrow();

        hero.learnPendingSkill(first);

        assertSame(newSkill, hero.getSkills().getFirst(), "la nuova prende il posto della dimenticata");
        assertFalse(hero.knows(first));
        assertTrue(hero.getPendingSkill().isEmpty());
        assertFalse(hero.getUsedPp().containsKey(first.name()));
        assertThrows(IllegalStateException.class, () -> hero.learnPendingSkill(newSkill), "niente più in attesa");
    }

    @Test
    void skippingKeepsTheOldSkills() {
        Hero hero = new Hero("Test", WARRIOR);
        hero.gainExp(EXP_TO_LV7);
        List<Skill> before = hero.getSkills();

        hero.skipPendingSkill();

        assertEquals(before, hero.getSkills());
        assertTrue(hero.getPendingSkill().isEmpty());
    }

    @Test
    void loadedSkillsIgnoreUnknownOrLockedNames() {
        Hero hero = new Hero("Test", WARRIOR, 9, 0, new Inventory(0, 0));
        hero.loadSkills(List.of("Spada Sacra", "Inesistente", "Colpo Devastante", "Fendente"));
        assertEquals(List.of("Spada Sacra", "Fendente"), hero.getSkillNames(), "Colpo Devastante è del Lv12");

        Hero old = new Hero("Test", WARRIOR, 9, 0, new Inventory(0, 0));
        old.loadSkills(List.of());
        assertEquals(Hero.MAX_SKILLS, old.getSkills().size(), "i salvataggi vecchi tengono le prime skill");
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
