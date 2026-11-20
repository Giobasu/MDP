package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;

/** Attacco base: non ha PP, si può usare sempre. */
public class Attack implements Action {

    @Override
    public String name() {
        return "Attacco";
    }

    @Override
    public String description() {
        return "Colpo base. Si può usare sempre, anche senza PP.";
    }

    @Override
    public String execute(Combatant user, Combatant target) {
        int damage = target.takeDamage(user.getAttack());
        return user.getName() + " attacca!\n" + target.getName() + " subisce " + damage + " danni.";
    }
}
