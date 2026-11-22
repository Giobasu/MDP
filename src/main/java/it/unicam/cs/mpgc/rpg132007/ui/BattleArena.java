package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.core.ResourceSprites;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.heroes.Hero;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

/**
 * Il campo di battaglia: nemico in alto a destra, eroe in basso a sinistra, i loro riquadri informazioni
 * e le animazioni (ingresso, mosse, colpi, barre). Mostra lo stato del modello, non decide nulla del flusso.
 */
class BattleArena extends GridPane {

    /** Quanto sopra il fondo poggiano i piedi dello sprite: a metà della pedana ASCII (3 righe). */
    private static final double SPRITE_ON_PLATFORM = 22;

    private final InfoBox enemyInfo = new InfoBox(false);
    private final InfoBox heroInfo = new InfoBox(true);
    private final StatBar exp = heroInfo.addBar("EXP", "fill-exp", false);
    private final StackPane enemyStage = new StackPane();
    private final StackPane heroStage = new StackPane();
    private final SpriteFit enemyFit = new SpriteFit(SPRITE_ON_PLATFORM);
    private final SpriteFit heroFit = new SpriteFit(SPRITE_ON_PLATFORM);
    private Node enemySprite = new Region();
    private Node heroSprite = new Region();
    private Hero hero;
    private Enemy enemy;
    private int shownHeroHp;
    private int shownEnemyHp;

    BattleArena() {
        enemyStage.getChildren().addAll(platform(), enemyFit);
        heroStage.getChildren().addAll(platform(), heroFit);
        enemyStage.setMinSize(0, 0);
        heroStage.setMinSize(0, 0);
        enemyStage.setAlignment(Pos.BOTTOM_CENTER);
        heroStage.setAlignment(Pos.BOTTOM_CENTER);

        getStyleClass().add("arena");
        setPadding(new Insets(10, 30, 10, 30));
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        getColumnConstraints().addAll(half, half);
        RowConstraints top = new RowConstraints();
        top.setPercentHeight(42);
        RowConstraints bottom = new RowConstraints();
        bottom.setPercentHeight(58);
        getRowConstraints().addAll(top, bottom);

        add(enemyInfo, 0, 0);
        add(enemyStage, 1, 0);
        add(heroStage, 0, 1);
        add(heroInfo, 1, 1);
        GridPane.setValignment(enemyInfo, VPos.TOP);
        GridPane.setMargin(enemyInfo, new Insets(14, 0, 0, 10));
        GridPane.setHalignment(heroInfo, HPos.RIGHT);
        GridPane.setValignment(heroInfo, VPos.BOTTOM);
        GridPane.setMargin(heroInfo, new Insets(0, 10, 18, 0));
    }

    /** La pedana sotto lo sprite: un disegno ASCII nelle risorse, come gli sprite. */
    private static Label platform() {
        Label platform = new Label(String.join("\n", ResourceSprites.load(BattleArena.class, "platform.txt")));
        platform.getStyleClass().add("platform");
        platform.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(platform, Pos.BOTTOM_CENTER);
        return platform;
    }

    /** Inizio di una battaglia: nuovi sprite che entrano scorrendo, barre portate subito ai valori attuali. */
    void enter(Hero hero, Enemy enemy) {
        this.hero = hero;
        this.enemy = enemy;
        // dimensioni "ideali": se lo spazio è meno, SpriteFit rimpicciolisce lo sprite invece di tagliarlo
        enemySprite = enemyFit.setSprite(Sprites.label(enemy.getAppearance(), 380, 190));
        heroSprite = heroFit.setSprite(Sprites.label(hero.getAppearance(), 420, 300));
        refresh(false);
        BattleAnimations.slideIn(enemySprite, 420);
        BattleAnimations.slideIn(heroSprite, -420);
    }

    /** Riquadri e barre allineati allo stato attuale del modello. */
    void refresh(boolean animate) {
        heroInfo.showIdentity(hero, false);
        enemyInfo.showIdentity(enemy, enemy.isBoss());
        shownHeroHp = hero.getHp();
        shownEnemyHp = enemy.getHp();
        heroInfo.hp().set(hero.getHp(), hero.getMaxHp(), animate);
        enemyInfo.hp().set(enemy.getHp(), enemy.getMaxHp(), animate);
        exp.set(hero.getExp(), Hero.expToNextLevel(hero.getLevel()), animate);
    }

    /** All'apertura di ogni messaggio: animazione della mossa e barre HP portate al valore del messaggio. */
    void animate(Message message) {
        Message.MoveUse move = message.move();
        if (move != null) {
            // la mossa appare sul lato del nemico se è l'eroe che attacca o il nemico che si potenzia
            boolean onEnemySide = move.byHero() == move.targetsOpponent();
            BattleAnimations.move(move.byHero(), !move.targetsOpponent(),
                    onEnemySide ? enemyStage : heroStage, move.byHero() ? heroSprite : enemySprite);
        }
        if (message.hasHp()) {
            animateHp(message.heroHp(), message.enemyHp());
        }
    }

    private void animateHp(int heroHp, int enemyHp) {
        if (enemyHp < shownEnemyHp) {
            BattleAnimations.hit(enemySprite, enemyHp == 0);
        }
        if (heroHp < shownHeroHp) {
            BattleAnimations.hit(heroSprite, heroHp == 0);
        }
        shownHeroHp = heroHp;
        shownEnemyHp = enemyHp;
        heroInfo.hp().set(heroHp, hero.getMaxHp(), true);
        enemyInfo.hp().set(enemyHp, enemy.getMaxHp(), true);
    }
}
