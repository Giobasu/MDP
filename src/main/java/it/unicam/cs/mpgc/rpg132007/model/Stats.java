package it.unicam.cs.mpgc.rpg132007.model;

/** Statistiche base di un combattente. */
public record Stats(int maxHp, int attack, int defense) {

    public Stats {
        if (maxHp < 1 || attack < 0 || defense < 0) {
            throw new IllegalArgumentException("Statistiche non valide: " + maxHp + "/" + attack + "/" + defense);
        }
    }
}
