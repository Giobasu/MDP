package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Element;

/** Una mossa eseguibile in battaglia. Nuove mosse = nuove classi che implementano questa interfaccia. */
public interface Action {

    String name();

    /** Esegue la mossa e ritorna il testo per il log: ogni riga ("\n") diventa un messaggio a sé. */
    String execute(Combatant user, Combatant target);

    /** Breve spiegazione mostrata nel menu delle mosse. */
    default String description() {
        return "";
    }

    /** Categoria mostrata nel menu (FISICO, PERFORANTE, STATO, CURA...). */
    default String category() {
        return "FISICO";
    }

    /** Elemento della mossa: decide i danni superefficaci e il colore dell'animazione. */
    default Element element() {
        return Element.NEUTRO;
    }

    /** true se la mossa colpisce l'avversario, false se agisce su chi la usa (cure, potenziamenti). */
    default boolean targetsEnemy() {
        return true;
    }

    /** Condizioni extra oltre ai PP (es. avere ancora pozioni). */
    default boolean isUsableBy(Combatant user) {
        return true;
    }
}
