package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Scelta della classe e del nome dell'eroe: un clic seleziona, doppio clic (o Invio) inizia. */
class HeroSelectView extends VBox {

    private static final int MAX_NAME_LENGTH = 16;

    private final List<VBox> cards = new ArrayList<>();
    private HeroClass selected;

    HeroSelectView(List<HeroClass> heroClasses, BiConsumer<String, HeroClass> onStart, Runnable onBack) {
        super(24);
        setAlignment(Pos.CENTER);

        Label title = new Label("Scegli il tuo eroe");
        title.getStyleClass().add("heading");

        TextField name = new TextField();
        name.setPromptText("Nome (facoltativo)");
        name.setMaxWidth(260);
        name.setTextFormatter(new TextFormatter<String>(
                change -> change.getControlNewText().length() <= MAX_NAME_LENGTH ? change : null));
        Runnable start = () -> onStart.accept(name.getText().isBlank() ? selected.name() : name.getText().strip(), selected);

        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        for (HeroClass heroClass : heroClasses) {
            VBox card = card(heroClass);
            card.setOnMouseClicked(e -> {
                select(heroClass, card);
                if (e.getClickCount() == 2) {
                    start.run();
                }
            });
            cards.add(card);
            row.getChildren().add(card);
        }
        select(heroClasses.getFirst(), cards.getFirst());

        Button play = new Button("Inizia");
        play.getStyleClass().add("primary");
        play.setDefaultButton(true);
        play.setOnAction(e -> start.run());
        Button back = new Button("Indietro");
        back.setOnAction(e -> onBack.run());
        HBox buttons = new HBox(10, back, play);
        buttons.setAlignment(Pos.CENTER);

        getChildren().addAll(title, row, name, buttons);
    }

    private void select(HeroClass heroClass, VBox card) {
        selected = heroClass;
        cards.forEach(c -> c.getStyleClass().remove("selected"));
        card.getStyleClass().add("selected");
    }

    private static VBox card(HeroClass heroClass) {
        Label name = new Label(heroClass.name().toUpperCase());
        name.getStyleClass().add("panel-title");

        SpriteFit spriteBox = new SpriteFit(0);
        spriteBox.setSprite(Sprites.label(heroClass.appearance(), 220, 170));
        spriteBox.setPrefHeight(170);

        Label description = new Label(heroClass.description());
        description.getStyleClass().add("muted");
        description.setWrapText(true);
        description.setAlignment(Pos.CENTER);
        description.setStyle("-fx-text-alignment: center;");

        VBox card = new VBox(10, name, spriteBox, description);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().addAll("panel", "selectable");
        card.setPrefWidth(260);
        return card;
    }
}
