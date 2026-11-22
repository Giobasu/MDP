package it.unicam.cs.mpgc.rpg132007.ui;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Riquadro con il bordo disegnato a caratteri ASCII ({@code +---+} e {@code |}) attorno a un contenuto.
 * Il bordo si ridisegna con tante colonne e righe quante ne entrano nello spazio disponibile,
 * così il riquadro può allargarsi e stringersi come un pannello normale.
 * Colore ({@code -fx-fill}) e font del bordo vengono dal CSS (classe "frame-border"); il riquadro ha la classe "frame".
 */
class AsciiFrame extends Region {

    /** Un Text e non un Label: un Label troppo piccolo taglierebbe il disegno con "...". */
    private final Text border = new Text();
    private final Node content;
    private int columns;
    private int rows;

    AsciiFrame(Node content) {
        this.content = content;
        getStyleClass().add("frame");
        border.getStyleClass().add("frame-border");
        border.setMouseTransparent(true);
        border.setTextOrigin(VPos.TOP);
        border.setManaged(false);
        getChildren().addAll(border, content);
    }

    @Override
    protected void layoutChildren() {
        double charW = charWidth();
        double lineH = lineHeight();
        int newColumns = Math.max(2, (int) (getWidth() / charW));
        int newRows = Math.max(2, (int) (getHeight() / lineH));
        if (newColumns != columns || newRows != rows) {
            columns = newColumns;
            rows = newRows;
            border.setText(draw(columns, rows));
        }
        // il bordo occupa un numero intero di caratteri: lo centro nello spazio che avanza
        double frameW = columns * charW;
        double frameH = rows * lineH;
        double x = (getWidth() - frameW) / 2;
        double y = (getHeight() - frameH) / 2;
        border.relocate(x, y);
        // il contenuto sta dentro il bordo: due caratteri di margine ai lati, una riga sopra e sotto
        layoutInArea(content, x + 2 * charW, y + lineH, frameW - 4 * charW, frameH - 2 * lineH,
                0, HPos.LEFT, VPos.TOP);
    }

    private static String draw(int columns, int rows) {
        String horizontal = "+" + "-".repeat(columns - 2) + "+";
        String middle = "|" + " ".repeat(columns - 2) + "|";
        StringBuilder sb = new StringBuilder(horizontal);
        for (int i = 0; i < rows - 2; i++) {
            sb.append('\n').append(middle);
        }
        return sb.append('\n').append(horizontal).toString();
    }

    private double charWidth() {
        return measure("M").getWidth();
    }

    private double lineHeight() {
        // altezza di due righe meno quella di una: è la distanza tra una riga e l'altra, interlinea compresa
        return measure("M\nM").getHeight() - measure("M").getHeight();
    }

    private Bounds measure(String text) {
        Text probe = new Text(text);
        Font font = border.getFont();
        probe.setFont(font);
        probe.setLineSpacing(border.getLineSpacing());
        return probe.getLayoutBounds();
    }

    @Override
    protected double computePrefWidth(double height) {
        return content.prefWidth(-1) + 4 * charWidth() + charWidth();
    }

    @Override
    protected double computePrefHeight(double width) {
        double inner = width < 0 ? -1 : Math.max(0, width - 5 * charWidth());
        return content.prefHeight(inner) + 2 * lineHeight() + lineHeight();
    }

    @Override
    protected double computeMinWidth(double height) {
        return content.minWidth(-1) + 5 * charWidth();
    }

    @Override
    protected double computeMinHeight(double width) {
        return content.minHeight(-1) + 3 * lineHeight();
    }
}
