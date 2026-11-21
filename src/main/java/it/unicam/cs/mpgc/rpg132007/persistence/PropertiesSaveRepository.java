package it.unicam.cs.mpgc.rpg132007.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/** Salva su un file .properties (formato chiave=valore della libreria standard). */
public class PropertiesSaveRepository implements SaveRepository {

    /** I PP di ogni mossa sono salvati come "pp.Nome della mossa=5"; i salvataggi vecchi non li hanno. */
    private static final String PP_PREFIX = "pp.";
    /** Le mosse conosciute sono salvate come "skills=Fendente,Muro di Scudi"; i salvataggi vecchi non le hanno. */
    private static final String SKILLS = "skills";
    private static final String SEPARATOR = ",";

    private final Path file;

    public PropertiesSaveRepository(Path file) {
        this.file = file;
    }

    public static PropertiesSaveRepository inUserHome() {
        return new PropertiesSaveRepository(Path.of(System.getProperty("user.home"), ".rpg132007", "save.properties"));
    }

    @Override
    public void save(SaveData data) throws IOException {
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
        Files.createDirectories(file.toAbsolutePath().getParent());
        // scrive su un file temporaneo e poi lo sostituisce: un crash a metà non corrompe il vecchio salvataggio
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp)) {
            props.store(writer, "Salvataggio RPG");
        }
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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

    @Override
    public Optional<SaveData> load() throws IOException {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            props.load(reader);
        }
        try {
            return Optional.of(new SaveData(
                    props.getProperty("name"),
                    props.getProperty("heroClass"),
                    Integer.parseInt(props.getProperty("level")),
                    Integer.parseInt(props.getProperty("exp")),
                    Integer.parseInt(props.getProperty("stage")),
                    Integer.parseInt(props.getProperty("gold")),
                    Integer.parseInt(props.getProperty("potions")),
                    readPp(props),
                    readSkills(props)));
        } catch (IllegalArgumentException e) {
            throw new IOException("Salvataggio corrotto o di una versione precedente: " + file, e);
        }
    }
}
