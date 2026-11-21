package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Healed;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Inventory;

import java.util.List;

/** Bere una pozione: consuma il turno, cura metà degli HP massimi. */
public class DrinkPotion implements Action {

    public static final int HEAL_PERCENT = 50;

    private final Inventory inventory;

    public DrinkPotion(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public String name() {
        return "Pozione";
    }

    @Override
    public String label() {
        return name() + "  x" + inventory.getPotions();
    }

    @Override
    public String description() {
        return "Recupera il " + HEAL_PERCENT + "% degli HP massimi. Consuma il turno.";
    }

    @Override
    public String category() {
        return "OGGETTO";
    }

    @Override
    public boolean targetsEnemy() {
        return false;
    }

    @Override
    public boolean isUsableBy(Combatant user) {
        return inventory.getPotions() > 0 && user.getHp() < user.getMaxHp();
    }

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        if (!inventory.usePotion()) {
            throw new IllegalStateException("Nessuna pozione: controllare isUsableBy prima di usarla");
        }
        int healed = user.heal(user.getMaxHp() * HEAL_PERCENT / 100);
        return List.of(new MoveUsed(user.getName(), name()), new Healed(user.getName(), healed));
    }
}
