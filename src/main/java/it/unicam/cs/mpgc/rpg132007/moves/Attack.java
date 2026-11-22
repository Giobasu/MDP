package it.unicam.cs.mpgc.rpg132007.moves;

import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.Damaged;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.core.Combatant;

import java.util.List;

/** Attacco base: non ha PP, si può usare sempre. */
public class Attack implements Action {

    @Override
    public String name() {
        return "Attacco";
    }

    @Override
    public String description() {
        return "Il colpo base: si può usare sempre, anche senza PP.";
    }

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        int damage = target.takeDamage(context.scale(user.getAttack()), false);
        return List.of(new MoveUsed(user.getName(), name()),
                new Damaged(target.getName(), damage, 1, context.critical()));
    }
}
