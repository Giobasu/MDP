package it.unicam.cs.mpgc.rpg132007.save;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundSaverTest {

    private static final int SAVES = 30;

    @TempDir
    Path dir;

    private static SaveData withGold(int gold) {
        return new SaveData("Test", "warrior", 1, 0, 1, gold, 0, Map.of(), List.of());
    }

    @Test
    void manySavesAtOnceLeaveTheMostRecentOnDisk() throws Exception {
        SaveRepository file = new JsonSaveRepository(dir.resolve("save.json"));
        BackgroundSaver saver = new BackgroundSaver(file);
        AtomicInteger saved = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        SaveListener counter = new SaveListener() {
            @Override
            public void saved() {
                saved.incrementAndGet();
            }

            @Override
            public void failed(IOException error) {
                failed.incrementAndGet();
            }
        };

        List<Thread> workers = new ArrayList<>();
        for (int gold = 0; gold < SAVES; gold++) {
            workers.add(saver.saveInBackground(withGold(gold), counter));
        }
        for (Thread worker : workers) {
            worker.join();
        }

        assertEquals(0, failed.get(), "nessun thread deve rompere il file degli altri");
        assertEquals(SAVES, saved.get(), "ogni salvataggio avvisa il suo listener");
        assertEquals(SAVES - 1, file.load().orElseThrow().gold(), "sul disco resta l'ultimo salvataggio chiesto");
    }

    @Test
    void failuresReachTheListener() throws InterruptedException {
        SaveRepository broken = new SaveRepository() {
            @Override
            public void save(SaveData data) throws IOException {
                throw new IOException("disco pieno");
            }

            @Override
            public Optional<SaveData> load() {
                return Optional.empty();
            }
        };
        List<String> errors = new ArrayList<>();
        Thread worker = new BackgroundSaver(broken).saveInBackground(withGold(0), new SaveListener() {
            @Override
            public void saved() {
                fail("il salvataggio non doveva riuscire");
            }

            @Override
            public void failed(IOException error) {
                errors.add(error.getMessage());
            }
        });
        worker.join(); // join garantisce anche che vediamo ciò che il thread ha scritto in errors

        assertEquals(List.of("disco pieno"), errors);
    }
}
