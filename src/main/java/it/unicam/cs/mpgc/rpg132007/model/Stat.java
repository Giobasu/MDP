package it.unicam.cs.mpgc.rpg132007.model;

/** Statistiche che le mosse possono alzare o abbassare durante una battaglia. */
public enum Stat {
    ATTACK("ATK"),
    DEFENSE("DEF");

    private final String shortName;

    Stat(String shortName) {
        this.shortName = shortName;
    }

    public String shortName() {
        return shortName;
    }
}
