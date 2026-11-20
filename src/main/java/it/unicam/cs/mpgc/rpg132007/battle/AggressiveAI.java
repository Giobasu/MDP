package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;

/** Attacca sempre. */
public class AggressiveAI implements EnemyAI {

    private final Action attack = new Attack();

    @Override
    public Action chooseAction(Enemy self, Combatant target) {
        return attack;
    }
}
