package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Damaged;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Drained;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.Healed;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.MoveUnavailable;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.MoveUsed;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatChanged;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusCured;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusDamage;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusInflicted;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusResisted;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.TurnSkipped;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Status;

/**
 * Tutti i testi della battaglia in italiano, in un solo posto.
 * Gli switch sono esaustivi: se si aggiunge un evento o uno stato il compilatore indica cosa tradurre.
 */
public class ItalianNarrator implements Narrator {

    @Override
    public String describe(BattleEvent event) {
        return switch (event) {
            case MoveUsed e -> e.user() + " usa " + e.move() + "!";
            case MoveUnavailable e -> "Non puoi usare " + e.move() + " adesso.";
            case Damaged e -> (e.critical() ? "Colpo critico! " : "") + e.target() + " subisce " + e.amount() + " danni"
                    + (e.hits() > 1 ? " (" + e.hits() + " colpi)" : "") + ".";
            case Healed e -> e.amount() > 0
                    ? e.who() + " recupera " + e.amount() + " HP."
                    : "Gli HP di " + e.who() + " sono già al massimo.";
            case Drained e -> e.who() + " assorbe " + e.amount() + " HP!";
            case StatChanged e -> statChange(e);
            case StatusInflicted e -> e.who() + " " + inflicted(e.status()) + "!";
            case StatusResisted e -> "Non ha effetto su " + e.who() + "...";
            case TurnSkipped e -> e.who() + " " + skipped(e.cause());
            case StatusDamage e -> e.who() + " soffre per " + cause(e.status()) + ": " + e.amount() + " danni.";
            case StatusCured e -> e.who() + " " + cured(e.status()) + ".";
        };
    }

    private static String statChange(StatChanged e) {
        String subject = statName(e.stat()) + " di " + e.who();
        if (e.changed() == 0) {
            return subject + (e.wanted() > 0 ? " non può salire di più!" : " non può scendere di più!");
        }
        String strength = Math.abs(e.changed()) > 1 ? " molto" : "";
        return subject + (e.changed() > 0 ? " aumenta" : " diminuisce") + strength + "!";
    }

    private static String statName(Stat stat) {
        return switch (stat) {
            case ATTACK -> "L'attacco";
            case DEFENSE -> "La difesa";
        };
    }

    private static String inflicted(Status status) {
        return switch (status) {
            case VELENO -> "è avvelenato";
            case BRUCIATURA -> "si è bruciato";
            case PARALISI -> "è paralizzato";
            case SONNO -> "si è addormentato";
        };
    }

    private static String cured(Status status) {
        return switch (status) {
            case VELENO -> "non è più avvelenato";
            case BRUCIATURA -> "non brucia più";
            case PARALISI -> "non è più paralizzato";
            case SONNO -> "si sveglia";
        };
    }

    private static String skipped(Status status) {
        return switch (status) {
            case SONNO -> "dorme profondamente...";
            case PARALISI -> "è paralizzato! Non riesce a muoversi.";
            case VELENO, BRUCIATURA -> "non riesce a muoversi.";
        };
    }

    private static String cause(Status status) {
        return switch (status) {
            case VELENO -> "il veleno";
            case BRUCIATURA -> "la bruciatura";
            case PARALISI -> "la paralisi";
            case SONNO -> "il sonno";
        };
    }
}
