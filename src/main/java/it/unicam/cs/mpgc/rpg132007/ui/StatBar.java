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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Barra stile JRPG ("HP [██████░░░] 40/52") che scorre verso il nuovo valore invece di saltarci.
 * Se colorClass è null il colore dipende dalla percentuale (verde, giallo, rosso) come le barre HP classiche.
 */
class StatBar extends HBox {

    private final double width;
    private final String colorClass;
    private final Region fill = new Region();
    private final Label value = new Label();
    private final DoubleProperty shown = new SimpleDoubleProperty();
    private Timeline animation;
    private int max = 1;

    StatBar(String caption, String colorClass, boolean showValue, double width, double height) {
        super(8);
        this.width = width;
        this.colorClass = colorClass;
        setAlignment(Pos.CENTER_LEFT);

        Label tag = new Label(caption);
        tag.getStyleClass().add("bar-tag");
        tag.setMinWidth(34);

        fill.setMinWidth(0);
        fill.setMaxHeight(Double.MAX_VALUE);
        StackPane track = new StackPane(fill);
        track.getStyleClass().add("bar-track");
        track.setAlignment(Pos.CENTER_LEFT);
        track.setMinSize(width, height);
        track.setPrefSize(width, height);
        track.setMaxSize(width, height);

        value.getStyleClass().add("bar-value");
        value.setMinWidth(80);
        getChildren().addAll(tag, track);
        if (showValue) {
            getChildren().add(value);
        }
        shown.addListener((obs, old, now) -> redraw());
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
        fill.setMaxWidth(width * ratio);
        fill.setPrefWidth(width * ratio);
        String color = colorClass != null ? colorClass
                : ratio > 0.5 ? "fill-hp-high" : ratio > 0.2 ? "fill-hp-mid" : "fill-hp-low";
        fill.getStyleClass().setAll("bar-fill", color);
        value.setText(Math.round(shown.get()) + " / " + max);
    }
}
