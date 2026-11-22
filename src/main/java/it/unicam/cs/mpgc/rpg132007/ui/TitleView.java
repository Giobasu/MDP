package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.core.ResourceSprites;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Schermata iniziale: logo e menu principale. Logo e spada sono disegni ASCII nelle risorse, come gli sprite. */
class TitleView extends VBox {

    TitleView(String message, Runnable onNewGame, Runnable onLoad, Runnable onExit) {
        super(18);
        setAlignment(Pos.CENTER);

        Label logo = asciiArt("logo.txt");
        logo.getStyleClass().add("logo");
        Label subtitle = new Label("un JRPG a turni in stile ASCII");
        subtitle.getStyleClass().add("muted");
        Label sword = asciiArt("sword.txt");
        sword.getStyleClass().add("muted");

        Button newGame = menuButton("Nuova partita", onNewGame);
        newGame.getStyleClass().add("primary");
        Label status = new Label(message);
        status.getStyleClass().add("error");

        getChildren().addAll(logo, subtitle, sword, newGame,
                menuButton("Carica partita", onLoad), menuButton("Esci", onExit), status);
    }

    /** Un disegno ASCII letto da un file .txt accanto a questa classe nelle risorse. */
    private static Label asciiArt(String file) {
        return new Label(String.join("\n", ResourceSprites.load(TitleView.class, file)));
    }

    private static Button menuButton(String text, Runnable onClick) {
        Button b = new Button(text);
        b.setPrefWidth(240);
        b.setOnAction(e -> onClick.run());
        return b;
    }
}
