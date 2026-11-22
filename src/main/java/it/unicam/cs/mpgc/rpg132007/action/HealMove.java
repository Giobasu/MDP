package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Healed;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;

import java.util.List;

/** Cura chi la usa di una % degli HP massimi. Con gli HP già al massimo non si può usare, così non spreca il turno. */
public record HealMove(String name, int percentOfMaxHp) implements Action {

    public HealMove {
        if (percentOfMaxHp < 1 || percentOfMaxHp > 100) {
            throw new IllegalArgumentException("Percentuale di cura non valida: " + percentOfMaxHp);
        }
    }

    @Override
    public String description() {
        return "Recupera il " + percentOfMaxHp + "% degli HP massimi.";
    }

    @Override
    public String category() {
        return "CURA";
    }

    @Override
    public boolean targetsEnemy() {
        return false;
    }

    @Override
    public boolean isUsableBy(Combatant user) {
        return user.getHp() < user.getMaxHp();
    }

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        int healed = user.heal(user.getMaxHp() * percentOfMaxHp / 100);
        return List.of(new MoveUsed(user.getName(), name), new Healed(user.getName(), healed));
    }
}
