package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;

/** Guardia: dimezza il prossimo danno subito. La usano i nemici prudenti. */
public class Defend implements Action {

    @Override
    public String name() {
        return "Difesa";
    }

    @Override
    public String description() {
        return "Dimezza i danni fino al prossimo turno.";
    }

    @Override
    public String category() {
        return "STATO";
    }

    @Override
    public boolean targetsEnemy() {
        return false;
    }

    @Override
    public String execute(Combatant user, Combatant target) {
        user.setDefending(true);
        return user.getName() + " si mette in guardia.";
    }
}
