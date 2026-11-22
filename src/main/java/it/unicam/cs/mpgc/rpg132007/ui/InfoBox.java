package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.core.Combatant;
import it.unicam.cs.mpgc.rpg132007.core.Stat;
import it.unicam.cs.mpgc.rpg132007.core.Status;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Riquadro informazioni in stile ASCII: nome, livello, stato alterato, modificatori e barra HP. */
class InfoBox extends AsciiFrame {

    /** Lunghezza delle barre, in caratteri. */
    private static final int BAR_CELLS = 20;

    private final VBox body;
    private final Label name = new Label();
    private final Label level = new Label();
    private final Label status = new Label();
    private final Label stages = new Label();
    private final StatBar hp;

    InfoBox(boolean showHpNumbers) {
        this(new VBox(4), showHpNumbers);
    }

    private InfoBox(VBox body, boolean showHpNumbers) {
        super(body);
        this.body = body;
        getStyleClass().add("info-box");
        name.getStyleClass().add("info-name");
        level.getStyleClass().add("info-level");
        stages.getStyleClass().add("info-status");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, name, spacer, level);
        header.setAlignment(Pos.CENTER_LEFT);
        hp = new StatBar("HP", null, showHpNumbers, BAR_CELLS);
        HBox footer = new HBox(8, status, stages);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setMinHeight(18);
        body.getChildren().addAll(header, hp, footer);
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
    }

    /** Aggiunge un'altra barra sotto quella degli HP (es. EXP). */
    StatBar addBar(String caption, String colorClass, boolean showValue) {
        StatBar bar = new StatBar(caption, colorClass, showValue, BAR_CELLS);
        body.getChildren().add(body.getChildren().size() - 1, bar);
        return bar;
    }

    StatBar hp() {
        return hp;
    }

    /** Nome, livello e stati: cambiano solo a fine turno, gli HP invece li anima la battaglia. boss = bordo evidenziato. */
    void showIdentity(Combatant c, boolean boss) {
        name.setText(c.getName().toUpperCase());
        level.setText("Lv" + c.getLevel());
        getStyleClass().remove("boss");
        if (boss) {
            getStyleClass().add("boss");
        }
        Status s = c.getStatus();
        chip(status, s == null ? null : "[" + Labels.status(s) + "]", s == null ? "" : "st-" + s.name().toLowerCase());

        StringBuilder tags = new StringBuilder();
        for (Stat stat : Stat.values()) {
            int stage = c.getStage(stat);
            if (stage != 0) {
                tags.append(Labels.stat(stat)).append(stage > 0 ? "+" : "-").append(Math.abs(stage)).append("  ");
            }
        }
        stages.setText(tags.toString().strip());
    }

    /** Etichetta colorata; text null la nasconde. */
    private static void chip(Label label, String text, String colorClass) {
        label.setVisible(text != null);
        label.setManaged(text != null);
        label.setText(text == null ? "" : text);
        label.getStyleClass().setAll("label", "chip", colorClass);
    }
}
