package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.Defend;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;

/** Sotto il 30% di HP alterna guardia e attacco; altrimenti attacca. */
public class CautiousAI implements EnemyAI {

    private final Action attack = new Attack();
    private final Action defend = new Defend();

    @Override
    public Action chooseAction(Enemy self, Combatant target) {
        boolean lowHp = self.getHp() * 100 / self.getMaxHp() < 30;
        // isDefending() qui riflette ancora la mossa del turno precedente
        return lowHp && !self.isDefending() ? defend : attack;
    }
}
