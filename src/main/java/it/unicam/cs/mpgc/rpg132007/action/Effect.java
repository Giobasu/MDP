package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Drained;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Healed;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatChanged;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusInflicted;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusResisted;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Effetto secondario di una mossa (potenziamenti, veleno, risucchio...).
 * Si combinano con {@link #and}: una mossa nuova si scrive senza creare nuove classi.
 */
@FunctionalInterface
public interface Effect {

    Effect NONE = (user, target, damage) -> List.of();

    /** damageDealt è il danno appena inflitto (0 per le mosse di stato). Ritorna cosa è successo (vuoto se nulla). */
    List<BattleEvent> apply(Combatant user, Combatant target, int damageDealt);

    default Effect and(Effect next) {
        return (user, target, damage) -> {
            List<BattleEvent> all = new ArrayList<>(apply(user, target, damage));
            all.addAll(next.apply(user, target, damage));
            return all;
        };
    }

    static Effect raise(Stat stat, int stages) {
        return (user, target, damage) ->
                List.of(new StatChanged(user.getName(), stat, user.changeStage(stat, stages), stages));
    }

    static Effect lower(Stat stat, int stages) {
        return (user, target, damage) -> {
            if (!target.isAlive()) {
                return List.of();
            }
            return List.of(new StatChanged(target.getName(), stat, target.changeStage(stat, -stages), -stages));
        };
    }

    /** Prova ad applicare uno stato alterato al bersaglio (fallisce se ne ha già uno o è immune). */
    static Effect inflict(Status status, int turns) {
        return (user, target, damage) -> {
            if (!target.isAlive()) {
                return List.of();
            }
            if (target.inflict(status, turns)) {
                return List.of(new StatusInflicted(target.getName(), status));
            }
            return List.of(new StatusResisted(target.getName()));
        };
    }

    /** Cura chi attacca di una % del danno inflitto. */
    static Effect drain(int percent) {
        return (user, target, damage) -> {
            int healed = user.heal(damage * percent / 100);
            if (healed == 0) {
                return List.of();
            }
            return List.of(new Drained(user.getName(), healed));
        };
    }

    static Effect heal(int percentOfMaxHp) {
        return (user, target, damage) ->
                List.of(new Healed(user.getName(), user.heal(user.getMaxHp() * percentOfMaxHp / 100)));
    }
}
