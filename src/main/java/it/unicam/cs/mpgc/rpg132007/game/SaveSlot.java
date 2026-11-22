package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClassCatalog;
import it.unicam.cs.mpgc.rpg132007.save.BackgroundSaver;
import it.unicam.cs.mpgc.rpg132007.save.SaveData;
import it.unicam.cs.mpgc.rpg132007.save.SaveListener;
import it.unicam.cs.mpgc.rpg132007.save.SaveRepository;

import java.io.IOException;
import java.util.Optional;

/**
 * Il salvataggio della partita: scrittura subito o in background, salvataggio automatico e caricamento.
 * Mette insieme il repository (dove si salva), il {@link SaveMapper} (cosa si salva) e il {@link BackgroundSaver} (in che thread).
 */
final class SaveSlot {

    /** Ciò che si ritrova caricando una partita. */
    record Loaded(Hero hero, int stage) {
    }

    private final SaveRepository repository;
    private final SaveMapper mapper;
    private final BackgroundSaver saver;
    /** Chi vuole sapere com'è andato il salvataggio automatico (di solito la view). */
    private SaveListener autoSaveListener = SaveListener.IGNORE;

    SaveSlot(SaveRepository repository, HeroClassCatalog heroClasses) {
        this.repository = repository;
        this.mapper = new SaveMapper(heroClasses);
        this.saver = new BackgroundSaver(repository);
    }

    /** Salva subito, nel thread di chi chiama. */
    void saveNow(Hero hero, int stage) throws IOException {
        saver.saveNow(mapper.toSaveData(hero, stage));
    }

    /**
     * Salva in un thread separato e avvisa il listener a scrittura finita.
     * La "fotografia" della partita si scatta qui, nel thread di chi chiama: il thread di salvataggio
     * lavora solo su dati immutabili, quindi la partita può continuare intanto senza race condition.
     */
    void saveInBackground(Hero hero, int stage, SaveListener listener) {
        saver.saveInBackground(mapper.toSaveData(hero, stage), listener);
    }

    /** Salvataggio in background che avvisa il listener del salvataggio automatico. */
    void autoSave(Hero hero, int stage) {
        saveInBackground(hero, stage, autoSaveListener);
    }

    void setAutoSaveListener(SaveListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener null: usare SaveListener.IGNORE");
        }
        autoSaveListener = listener;
    }

    /** Vuoto se non c'è nessun salvataggio. */
    Optional<Loaded> load() throws IOException {
        Optional<SaveData> found = repository.load();
        if (found.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Loaded(mapper.toHero(found.get()), found.get().stage()));
    }
}
