package it.unicam.cs.mpgc.rpg132007.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Salvataggio su un file. Qui c'è ciò che è uguale per tutti i formati (cartelle, file temporaneo, lettura);
 * le sottoclassi decidono solo come scrivere e leggere i dati ({@link #write} e {@link #read}).
 * Per un nuovo formato basta una nuova sottoclasse, senza toccare questa.
 */
public abstract class FileSaveRepository implements SaveRepository {

    private final Path file;

    protected FileSaveRepository(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("Il file di salvataggio non può essere null");
        }
        this.file = file;
    }

    /** Scrive su un file temporaneo e poi lo sostituisce: un crash a metà non corrompe il vecchio salvataggio. */
    @Override
    public final void save(SaveData data) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            write(data, writer);
        }
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public final Optional<SaveData> load() throws IOException {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return Optional.of(read(reader));
        }
    }

    /** Scrive i dati nel formato della sottoclasse. */
    protected abstract void write(SaveData data, Writer writer) throws IOException;

    /** Legge i dati; se il contenuto non è valido lancia IOException con un messaggio chiaro. */
    protected abstract SaveData read(Reader reader) throws IOException;

    /** Per i messaggi d'errore. */
    protected Path file() {
        return file;
    }
}
