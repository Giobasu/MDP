package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Inventory;

import java.util.List;

/** Bere una pozione: consuma una pozione dell'inventario e il turno; la cura vera e propria è una {@link HealMove}. */
public class DrinkPotion implements Action {

    public static final int HEAL_PERCENT = 50;

    private final Inventory inventory;
    private final HealMove heal = new HealMove("Pozione", HEAL_PERCENT);

    public DrinkPotion(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public String name() {
        return heal.name();
    }

    @Override
    public String label() {
        return name() + "  x" + inventory.getPotions();
    }

    @Override
    public String description() {
        return heal.description();
    }

    @Override
    public String category() {
        return "OGGETTO";
    }

    @Override
    public boolean targetsEnemy() {
        return heal.targetsEnemy();
    }

    @Override
    public boolean isUsableBy(Combatant user) {
        return inventory.getPotions() > 0 && heal.isUsableBy(user);
    }

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        if (!inventory.usePotion()) {
            throw new IllegalStateException("Nessuna pozione: controllare isUsableBy prima di usarla");
        }
        return heal.execute(user, target, context);
    }
}
