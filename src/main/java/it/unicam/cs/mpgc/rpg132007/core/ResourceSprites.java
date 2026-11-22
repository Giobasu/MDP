package it.unicam.cs.mpgc.rpg132007.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Legge gli sprite ASCII (.txt) dalle risorse del programma. */
public final class ResourceSprites {

    private ResourceSprites() {
    }

    /** Legge il file accanto alla classe anchor, togliendo righe vuote in cima e in fondo. */
    public static List<String> load(Class<?> anchor, String file) {
        try (InputStream in = anchor.getResourceAsStream(file)) {
            if (in == null) {
                throw new IllegalStateException("Sprite mancante: " + file);
            }
            List<String> lines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().map(String::stripTrailing).toList();
            int from = 0;
            int to = lines.size();
            while (from < to && lines.get(from).isEmpty()) {
                from++;
            }
            while (to > from && lines.get(to - 1).isEmpty()) {
                to--;
            }
            return lines.subList(from, to);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
