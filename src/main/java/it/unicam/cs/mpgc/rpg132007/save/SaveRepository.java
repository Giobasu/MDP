package it.unicam.cs.mpgc.rpg132007.save;

import java.io.IOException;
import java.util.Optional;

/** Dove e come si salva la partita. Oggi un file, domani un database o un server. */
public interface SaveRepository {

    void save(SaveData data) throws IOException;

    /** Vuoto se non esiste ancora un salvataggio. */
    Optional<SaveData> load() throws IOException;
}
