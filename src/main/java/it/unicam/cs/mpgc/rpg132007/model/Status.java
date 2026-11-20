package it.unicam.cs.mpgc.rpg132007.model;

/** Stati alterati. Come nei JRPG classici un combattente ne può avere uno solo alla volta. */
public enum Status {
    VELENO("VELENO", "è avvelenato", "non è più avvelenato", 6),
    BRUCIATURA("BRUCIATO", "si è bruciato", "non brucia più", 5),
    PARALISI("PARALISI", "è paralizzato", "non è più paralizzato", 0),
    SONNO("SONNO", "si è addormentato", "si sveglia", 0);

    /** Probabilità (%) che la paralisi faccia saltare il turno. */
    public static final int PARALYSIS_SKIP_CHANCE = 30;

    private final String label;
    private final String inflicted;
    private final String cured;
    private final int damagePercent;

    Status(String label, String inflicted, String cured, int damagePercent) {
        this.label = label;
        this.inflicted = inflicted;
        this.cured = cured;
        this.damagePercent = damagePercent;
    }

    public String label() {
        return label;
    }

    /** Es. "è avvelenato": da attaccare al nome ("Goblin è avvelenato!"). */
    public String inflictedText() {
        return inflicted;
    }

    public String curedText() {
        return cured;
    }

    /** % degli HP massimi persi a fine turno (0 se lo stato non fa danni). */
    public int damagePercent() {
        return damagePercent;
    }
}
