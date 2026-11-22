package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.battle.Narrator;

/**
 * Narratore dell'intera partita: oltre agli eventi di battaglia ({@link Narrator}) racconta quelli della partita.
 * La battaglia riceve solo un {@link Narrator}, perché degli eventi della partita non sa nulla (ISP).
 */
public interface GameNarrator extends Narrator {

    String describe(GameEvent event);
}
