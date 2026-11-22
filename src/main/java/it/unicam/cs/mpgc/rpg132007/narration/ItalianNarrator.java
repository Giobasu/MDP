package it.unicam.cs.mpgc.rpg132007.narration;

import it.unicam.cs.mpgc.rpg132007.game.GameEvent;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.AutoSaveStarted;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.Defeated;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.EnemyDefeated;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.ExpGained;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.GoldFound;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.LevelReached;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRefill;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRestored;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillForgotten;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillLearned;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillSkipped;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillWaiting;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SpringFound;
import it.unicam.cs.mpgc.rpg132007.game.GameNarrator;
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
 * Tutti i testi del gioco (battaglia e partita) in italiano, in un solo posto.
 * Gli switch sugli eventi sono esaustivi: se si aggiunge un evento il compilatore indica cosa tradurre.
 * Per un'altra lingua basta un'altra implementazione di {@link GameNarrator}.
 */
public class ItalianNarrator implements GameNarrator {

    @Override
    public String describe(BattleEvent event) {
        return switch (event) {
            case MoveUsed e -> e.user() + " usa " + e.move() + "!";
            case MoveUnavailable e -> "Non puoi usare " + e.move() + " adesso.";
            case Damaged e -> (e.critical() ? "Colpo critico! " : "") + e.target() + " subisce " + e.amount() + " danni"
                    + (e.hits() > 1 ? " (" + e.hits() + " colpi)" : "") + ".";
            case Healed e -> e.who() + " recupera " + e.amount() + " HP.";
            case Drained e -> e.who() + " assorbe " + e.amount() + " HP!";
            case StatChanged e -> statChange(e);
            case StatusInflicted e -> e.who() + " " + inflicted(e.status()) + "!";
            case StatusResisted e -> "Non ha effetto su " + e.who() + "...";
            case TurnSkipped e -> e.who() + " " + skipped(e.cause());
            case StatusDamage e -> e.who() + " soffre per " + statusName(e.status()) + ": " + e.amount() + " danni.";
            case StatusCured e -> e.who() + " " + cured(e.status()) + ".";
        };
    }

    @Override
    public String describe(GameEvent event) {
        return switch (event) {
            case EnemyDefeated e -> e.enemy() + " è stato sconfitto!";
            case GoldFound e -> "Trovi " + e.gold() + " monete d'oro.";
            case ExpGained e -> "Ottieni " + e.exp() + " EXP.";
            case LevelReached e -> "LEVEL UP! Ora sei al livello " + e.level() + ".";
            case SkillLearned e -> e.hero() + " impara " + e.skill() + "!";
            case SkillWaiting e -> e.hero() + " vuole imparare " + e.skill() + ", ma conosce già "
                    + e.maxSkills() + " mosse.";
            case SkillForgotten e -> "1, 2 e... puf! " + e.hero() + " dimentica " + e.skill() + "...";
            case SkillSkipped e -> e.hero() + " non impara " + e.skill() + ".";
            case PpRestored e -> ppRestored(e.reason());
            case AutoSaveStarted e -> "Salvataggio automatico in corso...";
            case Defeated e -> "Sei stato sconfitto... riprova lo stage " + e.stage() + ".";
            case SpringFound e -> "Trovi una fonte limpida e ti riposi.";
        };
    }

    private static String ppRestored(PpRefill reason) {
        return switch (reason) {
            case SPRING -> "I PP di tutte le mosse sono stati ricaricati!";
            case BOSS -> "Il boss è caduto: i PP di tutte le mosse sono stati ricaricati!";
            case DEFEAT -> "Ti riposi: i PP delle mosse sono stati ricaricati.";
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

    private static String statusName(Status status) {
        return switch (status) {
            case POISON -> "il veleno";
            case BURN -> "la bruciatura";
            case PARALYSIS -> "la paralisi";
            case SLEEP -> "il sonno";
        };
    }

    private static String inflicted(Status status) {
        return switch (status) {
            case POISON -> "è avvelenato";
            case BURN -> "si è bruciato";
            case PARALYSIS -> "è paralizzato";
            case SLEEP -> "si è addormentato";
        };
    }

    private static String cured(Status status) {
        return switch (status) {
            case POISON -> "non è più avvelenato";
            case BURN -> "non brucia più";
            case PARALYSIS -> "non è più paralizzato";
            case SLEEP -> "si sveglia";
        };
    }

    /** Solo gli stati che fanno saltare il turno hanno una frase propria; per gli altri vale quella generica. */
    private static String skipped(Status status) {
        return switch (status) {
            case SLEEP -> "dorme profondamente...";
            case PARALYSIS -> "è paralizzato! Non riesce a muoversi.";
            default -> "non riesce a muoversi.";
        };
    }
}
