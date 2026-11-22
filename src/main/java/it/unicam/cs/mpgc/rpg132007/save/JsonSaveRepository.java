package it.unicam.cs.mpgc.rpg132007.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

/** Salva la partita in un file JSON leggibile, usando la libreria Gson. È il formato usato dal gioco. */
public class JsonSaveRepository extends FileSaveRepository {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonSaveRepository(Path file) {
        super(file);
    }

    /** Il salvataggio del gioco: ~/.rpg132007/save.json */
    public static JsonSaveRepository inUserHome() {
        return new JsonSaveRepository(Path.of(System.getProperty("user.home"), ".rpg132007", "save.json"));
    }

    @Override
    protected void write(SaveData data, Writer writer) {
        gson.toJson(data, writer);
    }

    @Override
    protected SaveData read(Reader reader) throws IOException {
        SaveData data;
        try {
            data = gson.fromJson(reader, SaveData.class);
        } catch (RuntimeException e) {
            // Gson segnala sia il JSON malformato sia i dati rifiutati dal costruttore di SaveData
            // (campi mancanti o fuori dai limiti) con eccezioni unchecked: le riportiamo come un unico errore
            throw new IOException("Salvataggio corrotto: " + file(), e);
        }
        if (data == null) {
            throw new IOException("Salvataggio vuoto: " + file());
        }
        return data;
    }
}
