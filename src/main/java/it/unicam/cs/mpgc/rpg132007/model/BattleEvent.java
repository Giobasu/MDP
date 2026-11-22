package it.unicam.cs.mpgc.rpg132007.model;

/**
 * Qualcosa che è successo in battaglia, senza testo: le mosse dicono cosa fanno,
 * come raccontarlo (e in che lingua) lo decide un narratore.
 */
public sealed interface BattleEvent {

    record MoveUsed(String user, String move) implements BattleEvent {
    }

    record MoveUnavailable(String move) implements BattleEvent {
    }

    /** hits = colpi andati a segno (le mosse multiple si fermano se il bersaglio va KO); critical = colpo critico. */
    record Damaged(String target, int amount, int hits, boolean critical) implements BattleEvent {
    }

    record Healed(String who, int amount) implements BattleEvent {
    }

    record Drained(String who, int amount) implements BattleEvent {
    }

    /** changed = di quanto è cambiata davvero, wanted = di quanto doveva cambiare (diversi se si è al limite). */
    record StatChanged(String who, Stat stat, int changed, int wanted) implements BattleEvent {
    }


    record StatusInflicted(String who, Status status) implements BattleEvent {
    }

    record StatusResisted(String who) implements BattleEvent {
    }

    record TurnSkipped(String who, Status cause) implements BattleEvent {
    }

    record StatusDamage(String who, Status status, int amount) implements BattleEvent {
    }

    record StatusCured(String who, Status status) implements BattleEvent {
    }
}
