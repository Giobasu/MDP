package it.unicam.cs.mpgc.rpg132007.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Barra in stile ASCII ("HP [#######.....] 40/52") che scorre verso il nuovo valore invece di saltarci.
 * Se colorClass è null il colore dipende dalla percentuale (verde, giallo, rosso) come le barre HP classiche.
 */
class StatBar extends HBox {

    private static final char FULL = '#';
    private static final char EMPTY = '.';

    private final int cells;
    private final String colorClass;
    private final Label filled = new Label();
    private final Label empty = new Label();
    private final Label value = new Label();
    private final DoubleProperty shown = new SimpleDoubleProperty();
    private Timeline animation;
    private int max = 1;

    /** cells = quanti caratteri è lunga la barra. */
    StatBar(String caption, String colorClass, boolean showValue, int cells) {
        this.cells = cells;
        this.colorClass = colorClass;
        setAlignment(Pos.CENTER_LEFT);

        Label tag = new Label(String.format("%-4s", caption)); // stessa larghezza per HP ed EXP: le barre restano allineate
        tag.getStyleClass().add("bar-tag");
        tag.setMinWidth(Label.USE_PREF_SIZE);
        Label open = new Label("[");
        Label close = new Label("]");
        open.getStyleClass().add("bar-bracket");
        close.getStyleClass().add("bar-bracket");
        empty.getStyleClass().add("bar-empty");
        value.getStyleClass().add("bar-value");
        for (Label part : new Label[] {open, filled, empty, close, value}) {
            part.setMinWidth(Label.USE_PREF_SIZE);
        }
        getChildren().addAll(tag, open, filled, empty, close);
        if (showValue) {
            getChildren().add(value);
        }
        shown.addListener((obs, old, now) -> redraw());
        redraw();
    }

    /** Porta la barra a target; con animate=false salta subito (inizio battaglia). */
    void set(int target, int max, boolean animate) {
        this.max = Math.max(1, max);
        if (animation != null) {
            animation.stop();
        }
        if (!animate || Math.abs(shown.get() - target) < 0.5) {
            shown.set(target);
            redraw();
            return;
        }
        double millis = 250 + 650 * Math.min(1, Math.abs(shown.get() - target) / this.max);
        animation = new Timeline(new KeyFrame(Duration.millis(millis),
                new KeyValue(shown, target, Interpolator.EASE_OUT)));
        animation.play();
    }

    private void redraw() {
        double ratio = Math.clamp(shown.get() / max, 0, 1);
        // un valore sopra zero si vede sempre almeno con un carattere pieno
        int full = shown.get() > 0 ? Math.max(1, (int) Math.round(cells * ratio)) : 0;
        filled.setText(String.valueOf(FULL).repeat(full));
        empty.setText(String.valueOf(EMPTY).repeat(cells - full));
        String color = colorClass != null ? colorClass
                : ratio > 0.5 ? "fill-hp-high" : ratio > 0.2 ? "fill-hp-mid" : "fill-hp-low";
        filled.getStyleClass().setAll("label", "bar-fill", color);
        value.setText(" " + Math.round(shown.get()) + "/" + max);
    }
}
