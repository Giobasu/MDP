package it.unicam.cs.mpgc.rpg132007.model;

import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeroTest {

    private static final HeroClass WARRIOR = HeroClasses.byId("warrior").orElseThrow();

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
    void everySkillHasLimitedPp() {
        HeroClasses.ALL.forEach(c -> c.skills().forEach(s ->
                assertTrue(s.maxPp() > 0, c.name() + " / " + s.name() + " deve avere PP")));
    }

    @Test
    void everyClassHasUniqueIdAndValidStats() {
        assertEquals(HeroClasses.ALL.size(), HeroClasses.ALL.stream().map(HeroClass::id).distinct().count());
        HeroClasses.ALL.forEach(c -> assertDoesNotThrow(() -> new Hero("Test", c)));
    }

    @Test
    void inventoryNeverGoesNegative() {
        Inventory inv = new Inventory(10, 0);
        assertFalse(inv.spendGold(11));
        assertFalse(inv.usePotion());
        assertEquals(10, inv.getGold());
    }
}
