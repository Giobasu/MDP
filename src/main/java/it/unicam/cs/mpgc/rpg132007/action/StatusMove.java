package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;

import java.util.ArrayList;
import java.util.List;

/**
 * Mossa senza danno diretto: potenziamenti, indebolimenti, stati alterati (le cure sono {@link HealMove}). Cosa fa lo decide l'{@link Effect};
 * targetsEnemy dice solo se l'animazione va sull'avversario o su chi la usa.
 */
public record StatusMove(String name, String category, boolean targetsEnemy, String description,
                         Effect effect) implements Action {

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        List<BattleEvent> events = new ArrayList<>();
        events.add(new MoveUsed(user.getName(), name));
        events.addAll(effect.apply(user, target, 0));
        return events;
    }
}
