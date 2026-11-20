package it.unicam.cs.mpgc.rpg132007.model;

/** Statistiche che le mosse possono alzare o abbassare durante una battaglia. */
public enum Stat {
    ATTACK("ATK", "L'attacco"),
    DEFENSE("DEF", "La difesa");

    private final String shortName;
    private final String withArticle;

    Stat(String shortName, String withArticle) {
        this.shortName = shortName;
        this.withArticle = withArticle;
    }

    public String shortName() {
        return shortName;
    }

    /** Es. "L'attacco": serve a comporre i messaggi ("L'attacco di Goblin diminuisce!"). */
    public String withArticle() {
        return withArticle;
    }
}
