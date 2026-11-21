package it.unicam.cs.mpgc.rpg132007.enemies;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Reward;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import it.unicam.cs.mpgc.rpg132007.model.Status;

/**
 * Un nemico: attacca normalmente e ogni {@code specialEvery} turni usa la sua mossa speciale.
 * Ogni battaglia ha un nemico nuovo, quindi il conteggio dei turni riparte sempre da zero.
 */
public class Enemy extends Combatant {

    private final Action attack = new Attack();
    private final Reward reward;
    private final Action special;
    private final int specialEvery;
    private final boolean boss;
    private int turn;

    public Enemy(String name, int level, Stats stats, Appearance appearance, Reward reward,
                 Action special, int specialEvery, boolean boss) {
        super(name, level, stats, appearance);
        if (specialEvery < 1) {
            throw new IllegalArgumentException("specialEvery deve essere >= 1");
        }
        this.reward = reward;
        this.special = special;
        this.specialEvery = specialEvery;
        this.boss = boss;
    }

    /** La mossa di questo turno: la speciale ogni specialEvery turni, altrimenti l'attacco base. */
    public Action chooseAction() {
        turn++;
        return turn % specialEvery == 0 ? special : attack;
    }

    public Reward getReward() {
        return reward;
    }

    public boolean isBoss() {
        return boss;
    }

    /** I boss non si possono addormentare, altrimenti basterebbe una mossa per bloccarli. */
    @Override
    protected boolean isImmuneTo(Status s) {
        return boss && s == Status.SONNO;
    }
}
