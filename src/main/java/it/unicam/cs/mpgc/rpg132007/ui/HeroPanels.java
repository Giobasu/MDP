package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.Moveset;
import it.unicam.cs.mpgc.rpg132007.moves.Action;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** I pannelli informativi sull'eroe: dettagli di una mossa e schermata STATO. Costruiscono solo grafica, senza stato. */
final class HeroPanels {

    private HeroPanels() {
    }

    static Label detailTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("detail-type");
        return title;
    }

    static Label detailText(String text) {
        Label description = new Label(text);
        description.getStyleClass().add("detail-text");
        description.setWrapText(true);
        return description;
    }

    /** Dettagli di una mossa mentre la si sceglie: PP rimasti, categoria e descrizione. */
    static Node moveDetails(Hero hero, Action move, Runnable onBack) {
        String ppText = move.maxPp().isPresent()
                ? String.format("PP   %d / %d", hero.getMoveset().getPp(move), move.maxPp().getAsInt())
                : "PP   --";
        Label ppLine = new Label(ppText);
        ppLine.getStyleClass().add(hero.canUse(move) ? "detail-ok" : "detail-ko");
        Label back = new Label("[X / Esc] indietro");
        back.getStyleClass().add("hint");
        back.setOnMouseClicked(e -> onBack.run());
        return new VBox(6, ppLine, detailTitle(Labels.category(move.category())), detailText(move.description()), back);
    }

    /** STATO, parte sinistra: in ordine di livello le mosse conosciute e quelle ancora da sbloccare (le dimenticate non si vedono). */
    static Node moveList(Hero hero) {
        Moveset moveset = hero.getMoveset();
        GridPane moves = new GridPane();
        moves.setHgap(14);
        moves.setVgap(3);
        int row = 0;
        for (Skill skill : hero.getHeroClass().skills()) {
            boolean known = moveset.knows(skill);
            if (!known && skill.unlockLevel() <= hero.getLevel()) {
                continue;
            }
            Label name = new Label(known ? skill.name() : "Lv" + skill.unlockLevel() + "  ???");
            name.getStyleClass().add(known ? "status-move" : "status-locked");
            Label category = new Label(known ? Labels.category(skill.category()) : "");
            category.getStyleClass().add("status-locked");
            Label pp = new Label(known ? "PP " + moveset.getPp(skill) + "/" + skill.maxPp().orElse(0) : "");
            pp.getStyleClass().add("status-pp");
            moves.addRow(row++, name, category, pp);
        }
        return moves;
    }

    /** STATO, parte destra: classe, livello e statistiche. */
    static Label stats(Hero hero) {
        Label stats = new Label(String.format("%s  Lv%d%nHP %d/%d   EXP %d/%d%nATK %d   DEF %d",
                hero.getHeroClass().name().toUpperCase(), hero.getLevel(), hero.getHp(), hero.getMaxHp(),
                hero.getExp(), Hero.expToNextLevel(hero.getLevel()), hero.getAttack(), hero.getDefense()));
        stats.getStyleClass().add("status-stats");
        stats.setMinHeight(Region.USE_PREF_SIZE);
        return stats;
    }
}
