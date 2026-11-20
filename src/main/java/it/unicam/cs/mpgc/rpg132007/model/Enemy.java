package it.unicam.cs.mpgc.rpg132007.model;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.battle.EnemyAI;

/** Un nemico: ha una ricompensa e un'IA che decide le sue mosse. */
public class Enemy extends Combatant {

    private final Reward reward;
    private final EnemyAI ai;
    private final boolean boss;

    public Enemy(String name, int level, Stats stats, Appearance appearance, Reward reward, EnemyAI ai, boolean boss) {
        this(name, level, stats, appearance, reward, ai, boss, Element.NEUTRO);
    }

    public Enemy(String name, int level, Stats stats, Appearance appearance, Reward reward, EnemyAI ai, boolean boss,
                 Element element) {
        super(name, level, stats, appearance, element);
        this.reward = reward;
        this.ai = ai;
        this.boss = boss;
    }

    public Action chooseAction(Combatant target) {
        return ai.chooseAction(this, target);
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
