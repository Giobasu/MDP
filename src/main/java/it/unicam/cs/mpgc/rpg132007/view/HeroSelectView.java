package it.unicam.cs.mpgc.rpg132007.view;

import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.model.Stats;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Scelta della classe e del nome dell'eroe. Le carte sono generate da {@link HeroClasses#ALL}. */
class HeroSelectView extends VBox {

    private static final int MAX_NAME_LENGTH = 16;

    private final List<VBox> cards = new ArrayList<>();
    private HeroClass selected;

    HeroSelectView(BiConsumer<String, HeroClass> onStart, Runnable onBack) {
        super(20);
        setAlignment(Pos.CENTER);

        Label title = new Label("Scegli il tuo eroe");
        title.getStyleClass().add("heading");

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        for (HeroClass heroClass : HeroClasses.ALL) {
            VBox card = card(heroClass);
            card.setOnMouseClicked(e -> select(heroClass, card));
            cards.add(card);
            row.getChildren().add(card);
        }
        select(HeroClasses.ALL.getFirst(), cards.getFirst());

        TextField name = new TextField();
        name.setPromptText("Nome dell'eroe (facoltativo)");
        name.setMaxWidth(320);
        name.setTextFormatter(new TextFormatter<String>(
                change -> change.getControlNewText().length() <= MAX_NAME_LENGTH ? change : null));

        Button start = new Button("Inizia l'avventura");
        start.getStyleClass().add("primary");
        start.setDefaultButton(true);
        start.setOnAction(e -> onStart.accept(name.getText().isBlank() ? selected.name() : name.getText().strip(), selected));
        Button back = new Button("Indietro");
        back.setOnAction(e -> onBack.run());
        HBox buttons = new HBox(10, back, start);
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
        spriteBox.setSprite(Sprites.label(heroClass.appearance(), 250, 170));
        spriteBox.setPrefHeight(170);
        VBox.setVgrow(spriteBox, Priority.SOMETIMES);

        Label description = new Label(heroClass.description());
        description.getStyleClass().add("muted");
        description.setWrapText(true);

        Stats s = heroClass.statsFor(1);
        Label stats = new Label("HP " + s.maxHp() + "   ATK " + s.attack() + "   DEF " + s.defense());

        StringBuilder skills = new StringBuilder("Mosse:");
        for (Skill skill : heroClass.skills()) {
            skills.append(String.format("%n  Lv%-3d %-23s PP %d", skill.unlockLevel(), skill.name(), skill.maxPp()));
        }
        Label skillList = new Label(skills.toString());
        skillList.getStyleClass().add("skill-list");

        VBox card = new VBox(8, name, spriteBox, description, stats, skillList);
        card.getStyleClass().addAll("panel", "selectable");
        card.setPrefWidth(320);
        return card;
    }
}
