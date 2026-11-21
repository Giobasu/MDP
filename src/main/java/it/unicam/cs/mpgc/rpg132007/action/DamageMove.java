package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Damaged;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;

import java.util.ArrayList;
import java.util.List;

/**
 * Mossa offensiva configurabile: potenza in % dell'attacco, numero di colpi, se ignora la difesa
 * e un effetto secondario applicato dopo i colpi. Usata sia dagli eroi (dentro una {@link Skill})
 * sia come mossa speciale dei nemici.
 */
public record DamageMove(String name, String description, int powerPercent, int hits, boolean piercing,
                         Effect effect) implements Action {

    public DamageMove {
        if (powerPercent < 0 || hits < 1) {
            throw new IllegalArgumentException("Mossa non valida: " + name);
        }
    }

    @Override
    public String category() {
        return piercing ? "PERFORANTE" : "FISICO";
    }

    @Override
    public List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context) {
        int perHit = context.scale(user.getAttack() * powerPercent / 100);
        int total = 0;
        int landed = 0;
        while (landed < hits && target.isAlive()) {
            total += target.takeDamage(perHit, piercing);
            landed++;
        }
        List<BattleEvent> events = new ArrayList<>();
        events.add(new MoveUsed(user.getName(), name));
        events.add(new Damaged(target.getName(), total, landed, context.critical()));
        events.addAll(effect.apply(user, target, total));
        return events;
    }
}
