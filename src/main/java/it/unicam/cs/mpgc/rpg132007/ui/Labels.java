package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.core.Stat;
import it.unicam.cs.mpgc.rpg132007.core.Status;
import it.unicam.cs.mpgc.rpg132007.game.Route;
import it.unicam.cs.mpgc.rpg132007.moves.Category;

/**
 * Le etichette brevi con cui l'interfaccia mostra i valori del modello (stati, statistiche, tipi di mossa, sentieri).
 * Stanno qui e non negli enum perché sono una scelta di presentazione: un'altra interfaccia potrebbe usarne altre.
 */
final class Labels {

    private Labels() {
    }

    static String status(Status status) {
        return switch (status) {
            case POISON -> "VELENO";
            case BURN -> "BRUCIATO";
            case PARALYSIS -> "PARALISI";
            case SLEEP -> "SONNO";
        };
    }

    static String stat(Stat stat) {
        return switch (stat) {
            case ATTACK -> "ATK";
            case DEFENSE -> "DEF";
        };
    }

    static String category(Category category) {
        return switch (category) {
            case PHYSICAL -> "FISICO";
            case PIERCING -> "PERFORANTE";
            case STATUS -> "STATO";
            case HEAL -> "CURA";
            case ITEM -> "OGGETTO";
        };
    }

    static String route(Route route) {
        return switch (route) {
            case NORMAL -> "SENTIERO";
            case ELITE -> "SENTIERO ÉLITE";
            case SPRING -> "FONTE";
        };
    }

    static String routeDescription(Route route) {
        return switch (route) {
            case NORMAL -> "Un nemico normale. Ricompense normali.";
            case ELITE -> "Un nemico più forte del solito: EXP e oro doppi.";
            case SPRING -> "Nessuna battaglia: ricarica tutti i PP, ma non dà EXP né oro.";
        };
    }
}
