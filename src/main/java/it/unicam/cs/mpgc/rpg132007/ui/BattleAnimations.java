package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.core.ResourceSprites;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/** Le animazioni della battaglia: ingresso degli sprite, colpi subiti ed effetti ASCII delle mosse. */
final class BattleAnimations {

    /** Riga che separa un fotogramma dal successivo nei file .txt delle animazioni. */
    private static final String FRAME_SEPARATOR = "---";

    /** Fotogrammi ASCII di un colpo e delle scintille per cure e potenziamenti, letti dalle risorse. */
    private static final List<String> HIT_FRAMES = frames("hit.txt");
    private static final List<String> SUPPORT_FRAMES = frames("support.txt");

    private BattleAnimations() {
    }

    /**
     * Legge un'animazione da un file .txt accanto a questa classe: i fotogrammi sono separati da una riga "---".
     * Le righe vengono allungate tutte alla stessa larghezza, così il disegno non si sposta da un fotogramma all'altro
     * (gli editor tolgono gli spazi in fondo alle righe).
     */
    private static List<String> frames(String file) {
        List<String> lines = ResourceSprites.load(BattleAnimations.class, file);
        int width = lines.stream().mapToInt(String::length).max().orElse(0);
        List<String> frames = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            if (line.equals(FRAME_SEPARATOR)) {
                frames.add(String.join("\n", current));
                current.clear();
            } else {
                current.add(line + " ".repeat(width - line.length()));
            }
        }
        frames.add(String.join("\n", current));
        return List.copyOf(frames);
    }

    /** Lo sprite entra scorrendo da fromX. */
    static void slideIn(Node sprite, double fromX) {
        sprite.setOpacity(1);
        sprite.setTranslateY(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), sprite);
        slide.setFromX(fromX);
        slide.setToX(0);
        slide.play();
    }

    /** Lampeggio + scossone; se il colpo è fatale lo sprite scende e svanisce. */
    static void hit(Node sprite, boolean fatal) {
        FadeTransition blink = new FadeTransition(Duration.millis(70), sprite);
        blink.setFromValue(1);
        blink.setToValue(0.15);
        blink.setCycleCount(6);
        blink.setAutoReverse(true);
        TranslateTransition shake = new TranslateTransition(Duration.millis(45), sprite);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        ParallelTransition anim = new ParallelTransition(blink, shake);
        if (fatal) {
            anim.setOnFinished(e -> {
                TranslateTransition drop = new TranslateTransition(Duration.millis(450), sprite);
                drop.setToY(80);
                FadeTransition fade = new FadeTransition(Duration.millis(450), sprite);
                fade.setToValue(0);
                new ParallelTransition(drop, fade).play();
            });
        }
        anim.play();
    }

    /**
     * Animazione della mossa: chi attacca fa uno slancio, sul bersaglio compaiono i fotogrammi.
     * support = cura o potenziamento (scintille, nessuno slancio) invece di un colpo.
     */
    static void move(boolean fromHero, boolean support, StackPane target, Node attacker) {
        if (!support) {
            TranslateTransition lunge = new TranslateTransition(Duration.millis(110), attacker);
            lunge.setByX(fromHero ? 30 : -30);
            lunge.setAutoReverse(true);
            lunge.setCycleCount(2);
            lunge.play();
        }
        List<String> frames = support ? SUPPORT_FRAMES : HIT_FRAMES;
        Label overlay = new Label();
        overlay.setMouseTransparent(true);
        overlay.getStyleClass().addAll("fx", support ? "fx-support" : "fx-hit");
        StackPane.setAlignment(overlay, Pos.CENTER);
        target.getChildren().add(overlay);
        Timeline anim = new Timeline();
        for (int i = 0; i < frames.size(); i++) {
            String frame = frames.get(i);
            anim.getKeyFrames().add(new KeyFrame(Duration.millis(90 + 110 * i), e -> overlay.setText(frame)));
        }
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(90 + 110 * frames.size()),
                e -> target.getChildren().remove(overlay)));
        anim.play();
    }
}
