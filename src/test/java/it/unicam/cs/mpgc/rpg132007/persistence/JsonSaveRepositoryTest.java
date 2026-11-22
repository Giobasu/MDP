package it.unicam.cs.mpgc.rpg132007.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonSaveRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void saveThenLoadReturnsSameData() throws IOException {
        SaveRepository repo = new JsonSaveRepository(dir.resolve("save.json"));
        assertEquals(Optional.empty(), repo.load());

        SaveData data = new SaveData("Sir Àlvaro", "archer", 4, 12, 6, 80, 3, Map.of("Doppio Tiro", 7, "Squarcio", 0),
                List.of("Doppio Tiro", "Squarcio", "Freccia di Luce"));
        repo.save(data);

        assertEquals(Optional.of(data), repo.load());
    }

    @Test
    void malformedJsonIsReported() throws IOException {
        Path file = dir.resolve("save.json");
        Files.writeString(file, "{ questo non è json");

        assertThrows(IOException.class, () -> new JsonSaveRepository(file).load());
    }

    @Test
    void invalidDataIsReported() throws IOException {
        Path file = dir.resolve("save.json");
        Files.writeString(file, """
                { "name": "X", "heroClass": "warrior", "level": 0, "exp": 0, "stage": 1,
                  "gold": 0, "potions": 0, "usedPp": {}, "skills": [] }
                """);

        assertThrows(IOException.class, () -> new JsonSaveRepository(file).load(), "il livello 0 non è valido");
    }

    @Test
    void emptyFileIsReported() throws IOException {
        Path file = dir.resolve("save.json");
        Files.writeString(file, "");

        assertThrows(IOException.class, () -> new JsonSaveRepository(file).load());
    }
}
