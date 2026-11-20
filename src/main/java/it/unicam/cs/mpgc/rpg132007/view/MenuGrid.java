package it.unicam.cs.mpgc.rpg132007.view;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu a griglia con cursore "▶", come nei JRPG classici. Si usa con frecce + invio o col mouse.
 * Le voci sono Label e non Button, così il focus da tastiera resta alla schermata.
 */
class MenuGrid extends GridPane {

    /** onFocus (facoltativo) viene chiamato quando il cursore arriva sulla voce: serve per i dettagli delle mosse. */
    record Entry(String text, boolean enabled, Runnable onPick, Runnable onFocus) {

        Entry(String text, boolean enabled, Runnable onPick) {
            this(text, enabled, onPick, null);
        }
    }

    private final List<Entry> entries;
    private final List<Label> labels = new ArrayList<>();
    private final int columns;
    private int cursor = -1;

    MenuGrid(List<Entry> entries, int columns, int initialCursor) {
        this.entries = List.copyOf(entries);
        this.columns = columns;
        setHgap(28);
        setVgap(8);
        for (int i = 0; i < this.entries.size(); i++) {
            Entry entry = this.entries.get(i);
            Label label = new Label();
            label.getStyleClass().addAll("menu-item", entry.enabled() ? "enabled" : "disabled");
            int index = i;
            label.setOnMouseEntered(e -> select(index));
            label.setOnMouseClicked(e -> {
                e.consume(); // altrimenti il click arriva anche al riquadro e salta il messaggio appena aperto
                select(index);
                pick();
            });
            labels.add(label);
            add(label, i % columns, i / columns);
        }
        select(Math.clamp(initialCursor, 0, this.entries.size() - 1));
    }

    int cursor() {
        return cursor;
    }

    void move(int dx, int dy) {
        int next = cursor + dx + dy * columns;
        if (dx != 0 && next / columns != cursor / columns) {
            return; // niente salti di riga con sinistra/destra
        }
        if (next >= 0 && next < entries.size()) {
            select(next);
        }
    }

    void pick() {
        Entry entry = entries.get(cursor);
        if (entry.enabled()) {
            entry.onPick().run();
        }
    }

    private void select(int index) {
        cursor = index;
        for (int i = 0; i < labels.size(); i++) {
            labels.get(i).setText((i == index ? "▶ " : "  ") + entries.get(i).text());
        }
        Runnable onFocus = entries.get(index).onFocus();
        if (onFocus != null) {
            onFocus.run();
        }
    }
}
