package it.unicam.cs.mpgc.rpg132007.game;

/**
 * Ciò che succede nella partita fuori dai turni di battaglia (ricompense, livelli, mosse nuove, sentieri), senza testo:
 * come raccontarlo lo decide un {@link GameNarrator}, come per i {@code BattleEvent} della battaglia.
 */
public sealed interface GameEvent {

    /** Perché i PP di tutte le mosse sono stati ricaricati. */
    enum PpRefill { SPRING, BOSS, DEFEAT }

    record EnemyDefeated(String enemy) implements GameEvent {
    }

    record GoldFound(int gold) implements GameEvent {
    }

    record ExpGained(int exp) implements GameEvent {
    }

    record LevelReached(int level) implements GameEvent {
    }

    record SkillLearned(String hero, String skill) implements GameEvent {
    }

    /** La mossa si è sbloccata ma l'eroe ne conosce già maxSkills: dovrà dimenticarne una o rinunciare. */
    record SkillWaiting(String hero, String skill, int maxSkills) implements GameEvent {
    }

    record SkillForgotten(String hero, String skill) implements GameEvent {
    }

    record SkillSkipped(String hero, String skill) implements GameEvent {
    }

    record PpRestored(PpRefill reason) implements GameEvent {
    }

    record AutoSaveStarted() implements GameEvent {
    }

    record Defeated(int stage) implements GameEvent {
    }

    record SpringFound() implements GameEvent {
    }
}
