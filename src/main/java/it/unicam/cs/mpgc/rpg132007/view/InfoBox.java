package it.unicam.cs.mpgc.rpg132007.view;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Element;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Stat;
import it.unicam.cs.mpgc.rpg132007.model.Status;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Riquadro informazioni stile JRPG classico: nome, elemento, livello, stato alterato, modificatori e barra HP. */
class InfoBox extends VBox {

    private final Label name = new Label();
    private final Label element = new Label();
    private final Label level = new Label();
    private final Label status = new Label();
    private final Label stages = new Label();
    private final StatBar hp;

    InfoBox(boolean showHpNumbers) {
        super(6);
        getStyleClass().add("info-box");
        name.getStyleClass().add("info-name");
        level.getStyleClass().add("info-level");
        stages.getStyleClass().add("info-status");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, name, element, spacer, level);
        header.setAlignment(Pos.CENTER_LEFT);
        hp = new StatBar("HP", null, showHpNumbers, 200, 12);
        HBox footer = new HBox(8, status, stages);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setMinHeight(18);
        getChildren().addAll(header, hp, footer);
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
    }

    /** Aggiunge un'altra barra sotto quella degli HP (es. EXP). */
    StatBar addBar(String caption, String colorClass, boolean showValue, double height) {
        StatBar bar = new StatBar(caption, colorClass, showValue, 200, height);
        getChildren().add(getChildren().size() - 1, bar);
        return bar;
    }

    StatBar hp() {
        return hp;
    }

    /** Nome, livello e stati: cambiano solo a fine turno, gli HP invece li anima la battaglia. */
    void showIdentity(Combatant c) {
        name.setText(c.getName().toUpperCase());
        level.setText("Lv" + c.getLevel());
        boolean boss = c instanceof Enemy e && e.isBoss();
        getStyleClass().remove("boss");
        if (boss) {
            getStyleClass().add("boss");
        }
        chip(element, c.getElement() == Element.NEUTRO ? null : c.getElement().name(), elementClass(c.getElement()));
        Status s = c.getStatus();
        chip(status, s == null ? null : s.label(), s == null ? "" : "st-" + s.name().toLowerCase());

        StringBuilder tags = new StringBuilder();
        for (Stat stat : Stat.values()) {
            int stage = c.getStage(stat);
            if (stage != 0) {
                tags.append(stat.shortName()).append(stage > 0 ? "▲" : "▼").append(Math.abs(stage)).append("  ");
            }
        }
        if (c.isDefending()) {
            tags.append("GUARDIA");
        }
        stages.setText(tags.toString().strip());
    }

    static String elementClass(Element e) {
        return "el-" + e.name().toLowerCase();
    }

    /** Etichetta colorata; text null la nasconde. */
    static void chip(Label label, String text, String colorClass) {
        label.setVisible(text != null);
        label.setManaged(text != null);
        label.setText(text == null ? "" : text);
        label.getStyleClass().setAll("label", "chip", colorClass);
    }
}
