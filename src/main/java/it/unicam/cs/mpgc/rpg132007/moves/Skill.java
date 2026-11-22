package it.unicam.cs.mpgc.rpg132007.moves;

import it.unicam.cs.mpgc.rpg132007.core.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.core.Combatant;

import java.util.List;
import java.util.OptionalInt;

/**
 * Mossa di un eroe: avvolge una mossa qualsiasi aggiungendo il livello di sblocco e un numero limitato di usi (PP),
 * come nei JRPG classici. Tutto il resto lo delega alla mossa avvolta (decorator).
 */
public final class Skill implements Action {

    private final Action move;
    private final int unlockLevel;
    private final int maxPp;

    public Skill(Action move, int unlockLevel, int maxPp) {
        if (unlockLevel < 1 || maxPp < 1) {
            throw new IllegalArgumentException("Skill non valida: " + move.name());
        }
        this.move = move;
        this.unlockLevel = unlockLevel;
        this.maxPp = maxPp;
    }

    public Action move() {
        return move;
    }

    public int unlockLevel() {
        return unlockLevel;
    }

    @Override
    public OptionalInt maxPp() {
        return OptionalInt.of(maxPp);
    }

    @Override
    public String name() {
        return move.name();
    }

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        return move.execute(user, target, context);
    }

    @Override
    public String label() {
        return move.label();
    }

    @Override
    public String description() {
        return move.description();
    }

    @Override
    public Category category() {
        return move.category();
    }

    @Override
    public boolean targetsEnemy() {
        return move.targetsEnemy();
    }

    @Override
    public boolean isUsableBy(Combatant user) {
        return move.isUsableBy(user);
    }
}
