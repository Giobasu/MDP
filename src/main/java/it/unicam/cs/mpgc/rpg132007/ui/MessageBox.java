package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.core.Status;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Il testo del riquadro grande: scrive i messaggi una lettera alla volta, con la freccia "▼" lampeggiante,
 * e passa al successivo con {@link #advance()}. Non sa nulla di menu né di animazioni della battaglia.
 */
class MessageBox {

    private static final Duration TYPE_SPEED = Duration.millis(14);

    private final StackPane box;
    private final Label text = new Label();
    private final Label nextArrow = new Label("▼");
    private final Timeline arrowBlink = new Timeline(
            new KeyFrame(Duration.millis(450), e -> nextArrow.setOpacity(nextArrow.getOpacity() > 0 ? 0 : 1)));
    private final Deque<Message> queue = new ArrayDeque<>();
    private Consumer<Message> onMessage = m -> { };
    private Runnable afterMessages = () -> { };
    private Timeline typing;
    private String fullText = "";

    MessageBox(StackPane box) {
        this.box = box;
        text.getStyleClass().add("message");
        text.setWrapText(true);
        text.setAlignment(Pos.TOP_LEFT);
        text.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        nextArrow.getStyleClass().add("next-arrow");
        StackPane.setAlignment(nextArrow, Pos.BOTTOM_RIGHT);
        arrowBlink.setCycleCount(Timeline.INDEFINITE);
    }

    /** Mostra i messaggi uno per volta; onMessage parte all'apertura di ciascuno, then alla fine di tutti. */
    void play(List<Message> messages, Consumer<Message> onMessage, Runnable then) {
        queue.addAll(messages);
        this.onMessage = onMessage;
        this.afterMessages = then;
        next();
    }

    /** Testo fisso senza animazione (es. "Cosa farà l'eroe?"). */
    void prompt(String message) {
        text.getStyleClass().setAll("label", "message");
        text.setText(message);
        box.getChildren().setAll(text);
    }

    /** Invio / click: completa subito il testo in corso, oppure passa al messaggio successivo. */
    void advance() {
        if (typing != null && typing.getStatus() == Timeline.Status.RUNNING) {
            typingDone();
        } else {
            arrowBlink.stop();
            next();
        }
    }

    private void next() {
        Message message = queue.poll();
        if (message == null) {
            arrowBlink.stop();
            afterMessages.run();
            return;
        }
        onMessage.accept(message);
        type(message);
    }

    private void type(Message message) {
        fullText = message.text();
        text.setText("");
        text.getStyleClass().setAll("label", "message", "msg-" + message.tone().name().toLowerCase());
        nextArrow.setOpacity(0);
        box.getChildren().setAll(text, nextArrow);
        if (typing != null) {
            typing.stop();
        }
        typing = new Timeline(new KeyFrame(TYPE_SPEED, e -> {
            text.setText(fullText.substring(0, text.getText().length() + 1));
            if (text.getText().length() == fullText.length()) {
                typingDone();
            }
        }));
        typing.setCycleCount(fullText.length());
        typing.play();
    }

    private void typingDone() {
        typing.stop();
        text.setText(fullText);
        arrowBlink.play();
    }
}
