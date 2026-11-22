package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.core.Appearance;

import javafx.scene.control.Label;

import java.util.Locale;

/** Disegna uno sprite ASCII scegliendo la dimensione del font in modo che stia nello spazio dato. */
final class Sprites {

    // proporzioni tipiche di un carattere monospace rispetto alla dimensione del font
    private static final double CHAR_WIDTH = 0.6;
    private static final double LINE_HEIGHT = 1.2;
    private static final double MAX_FONT = 26;

    private Sprites() {
    }

    static Label label(Appearance look, double maxWidth, double maxHeight) {
        Label label = new Label(String.join("\n", look.sprite()));
        label.getStyleClass().add("sprite");
        int lines = Math.max(1, look.sprite().size());
        int columns = Math.max(1, look.sprite().stream().mapToInt(String::length).max().orElse(1));
        double size = Math.min(MAX_FONT, Math.min(maxHeight / (lines * LINE_HEIGHT), maxWidth / (columns * CHAR_WIDTH)));
        label.setStyle(String.format(Locale.ROOT, "-fx-font-size: %.1fpx; -fx-text-fill: %s;", size, look.color()));
        return label;
    }
}
