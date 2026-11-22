package it.unicam.cs.mpgc.rpg132007.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Contenitore che rimpicciolisce lo sprite quanto basta per farlo stare nello spazio disponibile,
 * appoggiato in basso e centrato. Un Label normale, se non ha spazio, taglia il testo con "...":
 * con gli sprite ASCII grandi su schermi piccoli (o con lo zoom di Windows) li mostrava a pezzi.
 */
class SpriteFit extends Pane {

    private final double bottomMargin;
    private Label sprite;

    SpriteFit(double bottomMargin) {
        this.bottomMargin = bottomMargin;
        setMinSize(0, 0);
    }

    /** Sostituisce lo sprite e lo ritorna, così chi chiama può animarlo. */
    Label setSprite(Label sprite) {
        this.sprite = sprite;
        sprite.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // mai troncato
        getChildren().setAll(sprite);
        requestLayout();
        return sprite;
    }

    @Override
    protected void layoutChildren() {
        if (sprite == null) {
            return;
        }
        double w = sprite.prefWidth(-1);
        double h = sprite.prefHeight(-1);
        double availableH = Math.max(1, getHeight() - bottomMargin);
        double scale = Math.min(1, Math.min(getWidth() / w, availableH / h));
        sprite.resize(w, h);
        sprite.setScaleX(scale);
        sprite.setScaleY(scale);
        // la scala avviene attorno al centro: sposto il box perché il bordo inferiore scalato tocchi il fondo
        sprite.relocate((getWidth() - w) / 2, getHeight() - bottomMargin - h / 2 - h * scale / 2);
    }

    @Override
    protected double computePrefWidth(double height) {
        return sprite == null ? 0 : sprite.prefWidth(-1);
    }

    @Override
    protected double computePrefHeight(double width) {
        return sprite == null ? 0 : sprite.prefHeight(-1) + bottomMargin;
    }
}
