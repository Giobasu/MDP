package it.unicam.cs.mpgc.rpg132007.ui;

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

/** Le animazioni della battaglia: ingresso degli sprite, colpi subiti ed effetti ASCII delle mosse. */
final class BattleAnimations {

    /** Fotogrammi ASCII di un colpo e delle scintille per cure e potenziamenti. */
    private static final String[] HIT_FRAMES = {
            "    /\n   / \n  /  ",
            "    /  /\n   /  / \n  /  /  ",
            " \\   /  /\n  \\ /  / \n   X  /  \n  / \\/   "};
    private static final String[] SUPPORT_FRAMES = {
            "   +   ",
            "  + +  \n   +   ",
            " +   + \n  + +  \n   +   ",
            "+     +\n +   + \n       "};

    private BattleAnimations() {
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
        String[] frames = support ? SUPPORT_FRAMES : HIT_FRAMES;
        Label overlay = new Label();
        overlay.setMouseTransparent(true);
        overlay.getStyleClass().addAll("fx", support ? "fx-support" : "fx-hit");
        StackPane.setAlignment(overlay, Pos.CENTER);
        target.getChildren().add(overlay);
        Timeline anim = new Timeline();
        for (int i = 0; i < frames.length; i++) {
            String frame = frames[i];
            anim.getKeyFrames().add(new KeyFrame(Duration.millis(90 + 110 * i), e -> overlay.setText(frame)));
        }
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(90 + 110 * frames.length),
                e -> target.getChildren().remove(overlay)));
        anim.play();
    }
}
