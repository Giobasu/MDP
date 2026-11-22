package it.unicam.cs.mpgc.rpg132007.moves;

/**
 * Ciò che la battaglia ha già deciso per la mossa in corso (oggi: se è un colpo critico).
 * Lo tira la battaglia, che ha il generatore casuale, così le mosse restano deterministiche e facili da testare.
 */
public record MoveContext(boolean critical) {

    /** Mossa senza critico: per i test e per chi esegue una mossa fuori dalla battaglia. */
    public static final MoveContext NORMAL = new MoveContext(false);

    /** Un colpo critico fa il 50% di danni in più. */
    public static final int CRITICAL_POWER_PERCENT = 150;

    /** Applica il critico (se c'è) al danno di un colpo. */
    public int scale(int damage) {
        return critical ? damage * CRITICAL_POWER_PERCENT / 100 : damage;
    }
}
