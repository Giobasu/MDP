package it.unicam.cs.mpgc.rpg132007.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Schermata iniziale: logo e menu principale. */
class TitleView extends VBox {

    // una lettera per riga di array: il logo viene composto affiancandole
    private static final String[][] LOGO_LETTERS = {
            {"   _   ", "  /_\\  ", " / _ \\ ", "/_/ \\_\\"},           // A
            {" ___ ", "/ __|", "\\__ \\", "|___/"},                    // S
            {"  ___ ", " / __|", "| (__ ", " \\___|"},                 // C
            {" ___ ", "|_ _|", " | | ", "|___|"},                     // I
            {" ___ ", "|_ _|", " | | ", "|___|"},                     // I
            {" ___ ", "| _ \\", "|  _/", "|_|  "},                    // P
            {"  ___  ", " / _ \\ ", "| (_) |", " \\___/ "},           // O
            {" _  _ ", "| \\| |", "| .` |", "|_|\\_|"}};              // N

    TitleView(String message, Runnable onNewGame, Runnable onLoad, Runnable onExit) {
        super(18);
        setAlignment(Pos.CENTER);

        Label logo = new Label(buildLogo());
        logo.getStyleClass().add("logo");
        Label subtitle = new Label("un JRPG a turni in stile ASCII");
        subtitle.getStyleClass().add("muted");
        Label sword = new Label("o==[]::::::::::::::::>");
        sword.getStyleClass().add("muted");

        Button newGame = menuButton("Nuova partita", onNewGame);
        newGame.getStyleClass().add("primary");
        Label status = new Label(message);
        status.getStyleClass().add("error");

        getChildren().addAll(logo, subtitle, sword, newGame,
                menuButton("Carica partita", onLoad), menuButton("Esci", onExit), status);
    }

    private static Button menuButton(String text, Runnable onClick) {
        Button b = new Button(text);
        b.setPrefWidth(240);
        b.setOnAction(e -> onClick.run());
        return b;
    }

    private static String buildLogo() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < LOGO_LETTERS[0].length; row++) {
            for (String[] letter : LOGO_LETTERS) {
                sb.append(letter[row]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
