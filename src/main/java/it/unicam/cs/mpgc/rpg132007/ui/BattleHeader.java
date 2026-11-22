package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.Inventory;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Barra in alto nella battaglia: stage (evidenziato se c'è un boss), esito dei salvataggi, classe, oro e pozioni. */
class BattleHeader extends HBox {

    private final Label stageLabel = new Label();
    private final Label saveStatus = new Label();
    private final Label purse = new Label();

    BattleHeader() {
        super(16);
        purse.getStyleClass().add("gold");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(stageLabel, spacer, saveStatus, purse);
        setPadding(new Insets(12, 22, 0, 22));
    }

    void update(int stage, boolean bossFight, Hero hero) {
        stageLabel.setText(bossFight ? "STAGE " + stage + "   !! BOSS !!" : "STAGE " + stage);
        stageLabel.getStyleClass().setAll("label", "stage");
        if (bossFight) {
            stageLabel.getStyleClass().add("boss");
        }
        Inventory inv = hero.getInventory();
        purse.setText(hero.getHeroClass().name() + "     Oro " + inv.getGold() + "     Pozioni " + inv.getPotions());
    }

    /** Va chiamato dal thread di JavaFX. */
    void showSaveStatus(String text, String style) {
        saveStatus.setText(text);
        saveStatus.getStyleClass().setAll("label", style);
    }
}
