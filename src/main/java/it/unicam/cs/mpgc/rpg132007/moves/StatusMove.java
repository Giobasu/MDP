package it.unicam.cs.mpgc.rpg132007.moves;

import it.unicam.cs.mpgc.rpg132007.core.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.core.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.core.Combatant;

import java.util.ArrayList;
import java.util.List;

/**
 * Mossa senza danno diretto: potenziamenti, indebolimenti, stati alterati (le cure sono {@link HealMove}). Cosa fa lo decide l'{@link Effect};
 * targetsEnemy dice solo se l'animazione va sull'avversario o su chi la usa.
 */
public record StatusMove(String name, boolean targetsEnemy, String description, Effect effect) implements Action {

    @Override
    public Category category() {
        return Category.STATUS;
    }

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        List<BattleEvent> events = new ArrayList<>();
        events.add(new MoveUsed(user.getName(), name));
        events.addAll(effect.apply(user, target, 0));
        return events;
    }
}
