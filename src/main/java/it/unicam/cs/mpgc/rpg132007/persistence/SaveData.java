package it.unicam.cs.mpgc.rpg132007.persistence;

import java.util.Map;

/** I dati minimi per ricostruire una partita. usedPp contiene solo le mosse che non hanno i PP al massimo. */
public record SaveData(String name, String heroClass, int level, int exp, int stage, int gold, int potions,
                       Map<String, Integer> usedPp) {

    public SaveData {
        if (name == null || name.isBlank() || heroClass == null || heroClass.isBlank()
                || level < 1 || exp < 0 || stage < 1 || gold < 0 || potions < 0) {
            throw new IllegalArgumentException("Dati di salvataggio non validi");
        }
        usedPp = Map.copyOf(usedPp);
    }

    /** Senza PP salvati: tutte le mosse ripartono al massimo. */
    public SaveData(String name, String heroClass, int level, int exp, int stage, int gold, int potions) {
        this(name, heroClass, level, exp, stage, gold, potions, Map.of());
    }
}
