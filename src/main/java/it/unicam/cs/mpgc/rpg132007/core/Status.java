package it.unicam.cs.mpgc.rpg132007.core;

import java.util.random.RandomGenerator;

/**
 * Stati alterati. Come nei JRPG classici un combattente ne può avere uno solo alla volta.
 * Ogni stato descrive da sé il proprio effetto (danno a fine turno, calo d'attacco, turni saltati):
 * per aggiungerne uno basta una nuova costante (più le sue frasi nel narratore e la sua etichetta nella view),
 * senza toccare la battaglia né i combattenti.
 */
public enum Status {
    //       danno%  attacco%  salta%  si consuma solo saltando il turno
    POISON(6, 100, 0, false),
    BURN(5, 75, 0, false),
    PARALYSIS(0, 100, 30, false),
    SLEEP(0, 100, 100, true);

    private static final int ALWAYS = 100;

    private final int damagePercent;
    private final int attackPercent;
    private final int skipChance;
    private final boolean countsDownOnSkip;

    Status(int damagePercent, int attackPercent, int skipChance, boolean countsDownOnSkip) {
        this.damagePercent = damagePercent;
        this.attackPercent = attackPercent;
        this.skipChance = skipChance;
        this.countsDownOnSkip = countsDownOnSkip;
    }

    /** % degli HP massimi persi a fine turno (0 se lo stato non fa danni). */
    public int damagePercent() {
        return damagePercent;
    }

    /** % dell'attacco che resta a chi ha questo stato (100 = nessun calo). */
    public int attackPercent() {
        return attackPercent;
    }

    /** Se questo turno va saltato. Il generatore si usa solo per gli stati con una probabilità parziale. */
    public boolean skipsTurn(RandomGenerator random) {
        return skipChance >= ALWAYS || (skipChance > 0 && random.nextInt(100) < skipChance);
    }

    /**
     * true se i turni dello stato si consumano solo quando fa saltare il turno (il sonno),
     * false se si consumano a fine turno (tutti gli altri).
     */
    public boolean countsDownOnSkip() {
        return countsDownOnSkip;
    }
}
