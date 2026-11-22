package it.unicam.cs.mpgc.rpg132007.game;

/** I sentieri tra uno stage e l'altro (tranne prima dei boss, dove la strada è una sola). */
public enum Route {
    /** Un nemico normale. */
    NORMAL,
    /** Un nemico più forte, con EXP e oro doppi. */
    ELITE,
    /** Nessuna battaglia: ricarica i PP e avanza di uno stage. Non si può prendere due volte di fila. */
    SPRING
}
