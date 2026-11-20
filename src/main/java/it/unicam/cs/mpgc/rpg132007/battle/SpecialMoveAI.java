package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;

/** Attacca normalmente e usa la mossa speciale ogni N turni. Ha uno stato: un'istanza per nemico. */
public class SpecialMoveAI implements EnemyAI {

    private final Action attack = new Attack();
    private final Action special;
    private final int every;
    private int turn;

    public SpecialMoveAI(Action special, int every) {
        if (every < 1) {
            throw new IllegalArgumentException("every deve essere >= 1");
        }
        this.special = special;
        this.every = every;
    }

    @Override
    public Action chooseAction(Enemy self, Combatant target) {
        turn++;
        return turn % every == 0 ? special : attack;
    }
}
