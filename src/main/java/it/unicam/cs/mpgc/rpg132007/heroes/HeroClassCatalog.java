package it.unicam.cs.mpgc.rpg132007.heroes;

import java.util.List;
import java.util.Optional;

/** Le classi giocabili disponibili. Oggi scritte nel codice, domani lette da file o scaricate. */
public interface HeroClassCatalog {

    List<HeroClass> all();

    default Optional<HeroClass> byId(String id) {
        return all().stream().filter(c -> c.id().equals(id)).findFirst();
    }
}
