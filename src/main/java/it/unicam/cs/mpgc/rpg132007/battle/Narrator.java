package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.core.BattleEvent;

/** Trasforma gli eventi di battaglia in frasi per il giocatore. Un'implementazione per lingua o stile. */
@FunctionalInterface
public interface Narrator {

    String describe(BattleEvent event);
}
