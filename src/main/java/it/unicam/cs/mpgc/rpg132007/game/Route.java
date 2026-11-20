package it.unicam.cs.mpgc.rpg132007.game;

/** I sentieri tra uno stage e l'altro (tranne prima dei boss, dove la strada è una sola). */
public enum Route {
    NORMAL("SENTIERO", "Un nemico normale. Ricompense normali."),
    ELITE("SENTIERO ÉLITE", "Un nemico più forte del solito: EXP e oro doppi."),
    SPRING("FONTE", "Nessuna battaglia: ricarica tutti i PP, ma non dà EXP né oro.");

    private final String label;
    private final String description;

    Route(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }
}
