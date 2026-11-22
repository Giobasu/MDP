package it.unicam.cs.mpgc.rpg132007.moves;

/**
 * Il tipo di una mossa. Il modello dice solo che tipo è: l'etichetta da mostrare (e in che lingua) la sceglie la view.
 */
public enum Category {
    /** Danno ridotto dalla difesa. */
    PHYSICAL,
    /** Danno che ignora la difesa. */
    PIERCING,
    /** Potenziamenti, indebolimenti, stati alterati. */
    STATUS,
    /** Cura chi la usa. */
    HEAL,
    /** Un oggetto dello zaino. */
    ITEM
}
