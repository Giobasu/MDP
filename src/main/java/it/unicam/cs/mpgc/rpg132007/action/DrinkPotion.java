package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Inventory;

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
    public String execute(Combatant user, Combatant target) {
        if (!inventory.usePotion()) {
            return user.getName() + " non ha pozioni!";
        }
        int healed = user.heal(user.getMaxHp() * HEAL_PERCENT / 100);
        return user.getName() + " beve una pozione!\n" + user.getName() + " recupera " + healed + " HP.";
    }
}
