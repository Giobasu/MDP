package it.unicam.cs.mpgc.rpg132007.moves;

import it.unicam.cs.mpgc.rpg132007.core.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.core.Combatant;

import java.util.List;
import java.util.OptionalInt;

/** Una mossa eseguibile in battaglia. Nuove mosse = nuove classi che implementano questa interfaccia. */
public interface Action {

    String name();

    /** Esegue la mossa e ritorna cosa è successo, nell'ordine. Il testo per il giocatore lo scrive il narratore. */
    List<BattleEvent> execute(Combatant user, Combatant target, MoveContext context);

    /** Testo della voce di menu (di base il nome; gli oggetti ci aggiungono la quantità). */
    default String label() {
        return name();
    }

    /** Breve spiegazione mostrata nel menu delle mosse. */
    default String description() {
        return "";
    }

    /** Tipo della mossa (fisica, perforante, di stato, cura...): come mostrarlo lo decide la view. */
    default Category category() {
        return Category.PHYSICAL;
    }

    /** true se la mossa colpisce l'avversario, false se agisce su chi la usa (cure, potenziamenti). */
    default boolean targetsEnemy() {
        return true;
    }

    /** Condizioni extra oltre ai PP (es. avere ancora pozioni). */
    default boolean isUsableBy(Combatant user) {
        return true;
    }

    /** PP massimi della mossa; vuoto se si può usare senza limiti (attacco base, mosse dei nemici). */
    default OptionalInt maxPp() {
        return OptionalInt.empty();
    }
}
