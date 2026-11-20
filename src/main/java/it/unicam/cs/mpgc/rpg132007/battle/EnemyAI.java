package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;

/** Strategia con cui un nemico sceglie la mossa del turno. */
@FunctionalInterface
public interface EnemyAI {

    Action chooseAction(Enemy self, Combatant target);
}
