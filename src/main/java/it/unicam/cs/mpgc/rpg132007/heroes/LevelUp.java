package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.moves.Skill;

import java.util.List;

/**
 * Esito di un guadagno di EXP: quanti livelli sono stati saliti, le skill imparate subito
 * e quelle sbloccate ma in attesa di un posto libero (vedi {@link Hero#getPendingSkill()}).
 */
public record LevelUp(int levelsGained, List<Skill> learned, List<Skill> waiting) {

    public LevelUp {
        if (levelsGained < 0) {
            throw new IllegalArgumentException("Livelli guadagnati negativi: " + levelsGained);
        }
        learned = List.copyOf(learned);
        waiting = List.copyOf(waiting);
    }
}
