package it.unicam.cs.mpgc.rpg132007.action;

/**
 * Una mossa speciale che si sblocca a un certo livello e si può usare un numero limitato di volte (PP),
 * come nei JRPG classici. I PP non si ricaricano tra una battaglia e l'altra: vedi {@link #UNLIMITED}.
 */
public interface Skill extends Action {

    /** maxPp per le mosse senza limite (le mosse speciali dei nemici). */
    int UNLIMITED = 0;

    int unlockLevel();

    int maxPp();
}
