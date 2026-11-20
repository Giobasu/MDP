package it.unicam.cs.mpgc.rpg132007.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesSaveRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void saveThenLoadReturnsSameData() throws IOException {
        SaveRepository repo = new PropertiesSaveRepository(dir.resolve("save.properties"));
        assertEquals(Optional.empty(), repo.load());

        SaveData data = new SaveData("Sir Àlvaro", "mage", 4, 12, 6, 80, 3, Map.of("Doppio Tiro", 7, "Squarcio", 0));
        repo.save(data);

        assertEquals(Optional.of(data), repo.load());
    }

    @Test
    void corruptFileIsReported() throws IOException {
        Path file = dir.resolve("save.properties");
        Files.writeString(file, "level=abc\n");

        assertThrows(IOException.class, () -> new PropertiesSaveRepository(file).load());
    }
}
