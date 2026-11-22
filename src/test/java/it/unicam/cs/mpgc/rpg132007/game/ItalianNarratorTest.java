package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.Damaged;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.Healed;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.StatChanged;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.StatusDamage;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.StatusInflicted;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.TurnSkipped;
import it.unicam.cs.mpgc.rpg132007.core.Stat;
import it.unicam.cs.mpgc.rpg132007.core.Status;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.LevelReached;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRefill;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRestored;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillWaiting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItalianNarratorTest {

    private final GameNarrator narrator = new ItalianNarrator();

    @Test
    void describesMovesAndDamage() {
        assertEquals("Eroe usa Fendente!", narrator.describe(new MoveUsed("Eroe", "Fendente")));
        assertEquals("Goblin subisce 12 danni.", narrator.describe(new Damaged("Goblin", 12, 1, false)));
        assertEquals("Goblin subisce 20 danni (2 colpi).", narrator.describe(new Damaged("Goblin", 20, 2, false)));
        assertEquals("Colpo critico! Goblin subisce 18 danni.", narrator.describe(new Damaged("Goblin", 18, 1, true)));
    }

    @Test
    void describesStatChangesAndLimits() {
        assertEquals("L'attacco di Eroe aumenta molto!", narrator.describe(new StatChanged("Eroe", Stat.ATTACK, 2, 2)));
        assertEquals("La difesa di Goblin diminuisce!", narrator.describe(new StatChanged("Goblin", Stat.DEFENSE, -1, -1)));
        assertTrue(narrator.describe(new StatChanged("Eroe", Stat.ATTACK, 0, 2)).contains("non può salire"));
    }

    @Test
    void describesHealingAndStatuses() {
        assertEquals("Eroe recupera 10 HP.", narrator.describe(new Healed("Eroe", 10)));
        assertEquals("Goblin è avvelenato!", narrator.describe(new StatusInflicted("Goblin", Status.POISON)));
        assertEquals("Goblin dorme profondamente...", narrator.describe(new TurnSkipped("Goblin", Status.SLEEP)));
        assertEquals("Goblin soffre per la bruciatura: 4 danni.", narrator.describe(new StatusDamage("Goblin", Status.BURN, 4)));
    }

    @Test
    void describesGameEvents() {
        assertEquals("LEVEL UP! Ora sei al livello 3.", narrator.describe(new LevelReached(3)));
        assertEquals("Eroe vuole imparare Fendente, ma conosce già 4 mosse.",
                narrator.describe(new SkillWaiting("Eroe", "Fendente", 4)));
        assertTrue(narrator.describe(new PpRestored(PpRefill.BOSS)).contains("boss"));
    }
}
