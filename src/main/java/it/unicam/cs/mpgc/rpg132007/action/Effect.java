package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Status;

/**
 * Effetto secondario di una mossa (potenziamenti, veleno, risucchio...).
 * Si combinano con {@link #and}: una mossa nuova si scrive senza creare nuove classi.
 */
@FunctionalInterface
public interface Effect {

    Effect NONE = (user, target, damage) -> "";

    /** damageDealt è il danno appena inflitto (0 per le mosse di stato). Ritorna il testo per il log, "" se nulla. */
    String apply(Combatant user, Combatant target, int damageDealt);

    default Effect and(Effect next) {
        return (user, target, damage) -> {
            String first = apply(user, target, damage);
            String second = next.apply(user, target, damage);
            return first.isEmpty() || second.isEmpty() ? first + second : first + "\n" + second;
        };
    }

    static Effect raise(Stat stat, int stages) {
        return (user, target, damage) -> stageText(user, stat, user.changeStage(stat, stages), stages);
    }

    static Effect lower(Stat stat, int stages) {
        return (user, target, damage) ->
                target.isAlive() ? stageText(target, stat, target.changeStage(stat, -stages), -stages) : "";
    }

    /** Prova ad applicare uno stato alterato al bersaglio (fallisce se ne ha già uno o è immune). */
    static Effect inflict(Status status, int turns) {
        return (user, target, damage) -> {
            if (!target.isAlive()) {
                return "";
            }
            return target.inflict(status, turns)
                    ? target.getName() + " " + status.inflictedText() + "!"
                    : "Non ha effetto su " + target.getName() + "...";
        };
    }

    static Effect poison(int turns) {
        return inflict(Status.VELENO, turns);
    }

    /** Cura chi attacca di una % del danno inflitto. */
    static Effect drain(int percent) {
        return (user, target, damage) -> {
            int healed = user.heal(damage * percent / 100);
            return healed > 0 ? user.getName() + " assorbe " + healed + " HP!" : "";
        };
    }

    static Effect heal(int percentOfMaxHp) {
        return (user, target, damage) -> {
            int healed = user.heal(user.getMaxHp() * percentOfMaxHp / 100);
            return healed > 0
                    ? user.getName() + " recupera " + healed + " HP."
                    : "Gli HP di " + user.getName() + " sono già al massimo.";
        };
    }

    private static String stageText(Combatant who, Stat stat, int changed, int wanted) {
        String subject = stat.withArticle() + " di " + who.getName();
        if (changed == 0) {
            return subject + (wanted > 0 ? " non può salire di più!" : " non può scendere di più!");
        }
        String strength = Math.abs(changed) > 1 ? " molto" : "";
        return subject + (changed > 0 ? " aumenta" : " diminuisce") + strength + "!";
    }
}
