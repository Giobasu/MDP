package it.unicam.cs.mpgc.rpg132007.model;

/**
 * Elemento di una mossa o di un combattente.
 * FUOCO batte NATURA, NATURA batte ACQUA, ACQUA batte FUOCO; LUCE e OMBRA si battono a vicenda.
 * Un elemento resiste a se stesso. NEUTRO non ha vantaggi né svantaggi.
 */
public enum Element {
    NEUTRO, FUOCO, ACQUA, NATURA, LUCE, OMBRA;

    public static final int SUPER_EFFECTIVE = 150;
    public static final int NORMAL = 100;
    public static final int NOT_EFFECTIVE = 60;

    /** Moltiplicatore (in %) del danno di una mossa di questo elemento contro un bersaglio di elemento defender. */
    public int effectivenessAgainst(Element defender) {
        if (this == NEUTRO || defender == NEUTRO) {
            return NORMAL;
        }
        if (beats(defender)) {
            return SUPER_EFFECTIVE;
        }
        if (this == defender || defender.beats(this)) {
            return NOT_EFFECTIVE;
        }
        return NORMAL;
    }

    private boolean beats(Element other) {
        return switch (this) {
            case FUOCO -> other == NATURA;
            case NATURA -> other == ACQUA;
            case ACQUA -> other == FUOCO;
            case LUCE -> other == OMBRA;
            case OMBRA -> other == LUCE;
            case NEUTRO -> false;
        };
    }
}
