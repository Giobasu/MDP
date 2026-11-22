package it.unicam.cs.mpgc.rpg132007.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Salva su un file .properties (formato chiave=valore della libreria standard).
 * Formato alternativo a {@link JsonSaveRepository}: il gioco usa il JSON, questo resta come esempio
 * di come si cambia formato senza toccare il resto del programma.
 */
public class PropertiesSaveRepository extends FileSaveRepository {

    /** I PP di ogni mossa sono salvati come "pp.Nome della mossa=5"; i salvataggi vecchi non li hanno. */
    private static final String PP_PREFIX = "pp.";
    /** Le mosse conosciute sono salvate come "skills=Fendente,Muro di Scudi"; i salvataggi vecchi non le hanno. */
    private static final String SKILLS = "skills";
    private static final String SEPARATOR = ",";

    public PropertiesSaveRepository(Path file) {
        super(file);
    }

    @Override
    protected void write(SaveData data, Writer writer) throws IOException {
        Properties props = new Properties();
        props.setProperty("name", data.name());
        props.setProperty("heroClass", data.heroClass());
        props.setProperty("level", String.valueOf(data.level()));
        props.setProperty("exp", String.valueOf(data.exp()));
        props.setProperty("stage", String.valueOf(data.stage()));
        props.setProperty("gold", String.valueOf(data.gold()));
        props.setProperty("potions", String.valueOf(data.potions()));
        data.usedPp().forEach((move, left) -> props.setProperty(PP_PREFIX + move, String.valueOf(left)));
        props.setProperty(SKILLS, String.join(SEPARATOR, data.skills()));
        props.store(writer, "Salvataggio RPG");
    }

    @Override
    protected SaveData read(Reader reader) throws IOException {
        Properties props = new Properties();
        props.load(reader);
        try {
            return new SaveData(
                    props.getProperty("name"),
                    props.getProperty("heroClass"),
                    Integer.parseInt(props.getProperty("level")),
                    Integer.parseInt(props.getProperty("exp")),
                    Integer.parseInt(props.getProperty("stage")),
                    Integer.parseInt(props.getProperty("gold")),
                    Integer.parseInt(props.getProperty("potions")),
                    readPp(props),
                    readSkills(props));
        } catch (IllegalArgumentException e) {
            throw new IOException("Salvataggio corrotto o di una versione precedente: " + file(), e);
        }
    }

    private static Map<String, Integer> readPp(Properties props) {
        Map<String, Integer> pp = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(PP_PREFIX)) {
                pp.put(key.substring(PP_PREFIX.length()), Integer.parseInt(props.getProperty(key)));
            }
        }
        return pp;
    }

    private static List<String> readSkills(Properties props) {
        String value = props.getProperty(SKILLS, "");
        return value.isBlank() ? List.of() : List.of(value.split(SEPARATOR));
    }
}
