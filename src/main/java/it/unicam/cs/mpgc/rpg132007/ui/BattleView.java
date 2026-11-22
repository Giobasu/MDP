package it.unicam.cs.mpgc.rpg132007.ui;

import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.game.Game;
import it.unicam.cs.mpgc.rpg132007.game.Route;
import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.Inventory;
import it.unicam.cs.mpgc.rpg132007.moves.Action;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;
import it.unicam.cs.mpgc.rpg132007.save.SaveListener;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Ellipse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Schermata di battaglia in stile JRPG classico: nemico in alto a destra, eroe in basso a sinistra,
 * riquadro dei messaggi e menu a griglia (LOTTA / ZAINO / STATO / FUGA).
 * Questa classe si occupa di layout, menu e tastiera; il testo animato è di {@link MessageBox},
 * le animazioni di {@link BattleAnimations}. Le mosse disponibili le decide il modello.
 */
class BattleView extends BorderPane {

    private final Game game;
    private final Runnable onMenu;

    private final Label stageLabel = new Label();
    private final Label purse = new Label();
    private final Label saveStatus = new Label();
    private final InfoBox enemyInfo = new InfoBox(false);
    private final InfoBox heroInfo = new InfoBox(true);
    private final StatBar exp = heroInfo.addBar("EXP", "fill-exp", false, 5);
    private final StackPane enemyStage = new StackPane();
    private final StackPane heroStage = new StackPane();
    private final SpriteFit enemyFit = new SpriteFit(14);
    private final SpriteFit heroFit = new SpriteFit(14);
    private Node enemySprite = new Region();
    private Node heroSprite = new Region();

    /** Riquadro grande in basso a sinistra: testo dei messaggi oppure griglia delle mosse. */
    private final StackPane mainBox = new StackPane();
    /** Riquadro piccolo in basso a destra: menu principale oppure dettagli della mossa. */
    private final StackPane sideBox = new StackPane();
    private final MessageBox messages = new MessageBox(mainBox);

    private MenuGrid activeMenu;
    private Runnable onBack;
    private int lastMoveCursor;
    private int shownHeroHp;
    private int shownEnemyHp;

    BattleView(Game game, Runnable onMenu) {
        this.game = game;
        this.onMenu = onMenu;
        getStyleClass().add("battle");

        purse.getStyleClass().add("gold");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(16, stageLabel, spacer, saveStatus, purse);
        top.setPadding(new Insets(12, 22, 0, 22));
        setTop(top);
        setCenter(arena());
        setBottom(bottomPanel());

        setFocusTraversable(true);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        setOnMouseClicked(e -> requestFocus());
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                Platform.runLater(this::requestFocus);
            }
        });
        game.setAutoSaveListener(saveFeedback);
        startEncounter();
    }

    /**
     * Esito dei salvataggi in background, mostrato in alto. Viene chiamato dal thread di salvataggio:
     * JavaFX permette di toccare la grafica solo dal suo thread, quindi si passa da Platform.runLater.
     */
    private final SaveListener saveFeedback = new SaveListener() {
        @Override
        public void saved() {
            Platform.runLater(() -> showSaveStatus("Partita salvata", "save-ok"));
        }

        @Override
        public void failed(IOException error) {
            Platform.runLater(() -> showSaveStatus("Salvataggio non riuscito: " + error.getMessage(), "save-ko"));
        }
    };

    private void showSaveStatus(String text, String style) {
        saveStatus.setText(text);
        saveStatus.getStyleClass().setAll("label", style);
    }

    // ---------------------------------------------------------------- layout

    private GridPane arena() {
        enemyStage.getChildren().addAll(platform(), enemyFit);
        heroStage.getChildren().addAll(platform(), heroFit);
        enemyStage.setMinSize(0, 0);
        heroStage.setMinSize(0, 0);
        enemyStage.setAlignment(Pos.BOTTOM_CENTER);
        heroStage.setAlignment(Pos.BOTTOM_CENTER);

        GridPane arena = new GridPane();
        arena.getStyleClass().add("arena");
        arena.setPadding(new Insets(10, 30, 10, 30));
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        arena.getColumnConstraints().addAll(half, half);
        RowConstraints top = new RowConstraints();
        top.setPercentHeight(42);
        RowConstraints bottom = new RowConstraints();
        bottom.setPercentHeight(58);
        arena.getRowConstraints().addAll(top, bottom);

        arena.add(enemyInfo, 0, 0);
        arena.add(enemyStage, 1, 0);
        arena.add(heroStage, 0, 1);
        arena.add(heroInfo, 1, 1);
        GridPane.setValignment(enemyInfo, VPos.TOP);
        GridPane.setMargin(enemyInfo, new Insets(14, 0, 0, 10));
        GridPane.setHalignment(heroInfo, HPos.RIGHT);
        GridPane.setValignment(heroInfo, VPos.BOTTOM);
        GridPane.setMargin(heroInfo, new Insets(0, 10, 18, 0));
        return arena;
    }

    private static Ellipse platform() {
        Ellipse platform = new Ellipse(150, 20);
        platform.getStyleClass().add("platform");
        StackPane.setAlignment(platform, Pos.BOTTOM_CENTER);
        return platform;
    }

    private HBox bottomPanel() {
        mainBox.getStyleClass().add("textbox");
        mainBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(mainBox, Priority.ALWAYS);
        mainBox.setOnMouseClicked(e -> {
            if (activeMenu == null) {
                messages.advance();
            }
        });
        sideBox.getStyleClass().add("textbox");
        sideBox.setAlignment(Pos.CENTER_LEFT);
        sideBox.setMinWidth(330);
        sideBox.setPrefWidth(330);

        HBox bottom = new HBox(10, mainBox, sideBox);
        bottom.setPadding(new Insets(0, 18, 16, 18));
        bottom.setPrefHeight(190);
        bottom.setMinHeight(170);
        return bottom;
    }

    // ---------------------------------------------------------------- flusso della battaglia

    private void startEncounter() {
        Hero hero = game.getHero();
        Enemy enemy = game.getBattle().getEnemy();
        shownHeroHp = hero.getHp();
        shownEnemyHp = enemy.getHp();
        // dimensioni "ideali": se lo spazio è meno, SpriteFit rimpicciolisce lo sprite invece di tagliarlo
        enemySprite = enemyFit.setSprite(Sprites.label(enemy.getAppearance(), 380, 190));
        heroSprite = heroFit.setSprite(Sprites.label(hero.getAppearance(), 420, 300));
        refreshInfo(false);
        BattleAnimations.slideIn(enemySprite, 420);
        BattleAnimations.slideIn(heroSprite, -420);

        List<Message> intro = new ArrayList<>();
        intro.add(enemy.isBoss()
                ? new Message("Un boss sbarra la strada: " + enemy.getName() + "!", Tone.DANGER)
                : new Message("Stage " + game.getStage() + ": appare " + enemy.getName() + "!", Tone.INFO));
        intro.add(new Message("Avanti, " + hero.getName() + "!", Tone.HERO));
        showMessages(intro, this::showMainMenu);
    }

    private void showMainMenu() {
        messages.prompt("Cosa farà " + game.getHero().getName() + "?");
        showMenu(sideBox, new MenuGrid(List.of(
                new MenuGrid.Entry("LOTTA", true, this::showMoves),
                new MenuGrid.Entry("ZAINO", true, this::showBag),
                new MenuGrid.Entry("STATO", true, this::showStatus),
                new MenuGrid.Entry("FUGA", true, this::flee)), 2, 0), null);
    }

    private void showMoves() {
        List<MenuGrid.Entry> entries = actionEntries(game.getHero().getMoves(), move -> {
            lastMoveCursor = activeMenu.cursor();
            play(move);
        });
        showMenu(mainBox, new MenuGrid(entries, 2, lastMoveCursor), this::showMainMenu);
    }

    private void showBag() {
        showMenu(mainBox, new MenuGrid(actionEntries(game.getHero().getItems(), this::play), 1, 0), this::showMainMenu);
    }

    /** Una voce di menu per azione: disattivata se non si può usare, con i dettagli a destra quando è selezionata. */
    private List<MenuGrid.Entry> actionEntries(List<Action> actions, Consumer<Action> onPick) {
        Hero hero = game.getHero();
        List<MenuGrid.Entry> entries = new ArrayList<>();
        for (Action action : actions) {
            entries.add(new MenuGrid.Entry(action.label(), hero.canUse(action),
                    () -> onPick.accept(action), () -> showDetails(action)));
        }
        return entries;
    }

    private static Label detailTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("detail-type");
        return title;
    }

    private static Label detailText(String text) {
        Label description = new Label(text);
        description.getStyleClass().add("detail-text");
        description.setWrapText(true);
        return description;
    }

    /** Riquadro a destra mentre si sceglie una mossa: PP rimasti, categoria e descrizione. */
    private void showDetails(Action move) {
        Hero hero = game.getHero();
        Label category = detailTitle(move.category());
        String ppText = move.maxPp().isPresent()
                ? String.format("PP   %d / %d", hero.getPp(move), move.maxPp().getAsInt())
                : "PP   --";
        Label ppLine = new Label(ppText);
        ppLine.getStyleClass().add(hero.canUse(move) ? "detail-ok" : "detail-ko");
        Label description = detailText(move.description());
        Label back = new Label("[X / Esc] indietro");
        back.getStyleClass().add("hint");
        back.setOnMouseClicked(e -> goBack());
        sideBox.getChildren().setAll(new VBox(6, ppLine, category, description, back));
    }

    private void showAfterBattleMenu() {
        boolean won = game.getBattle().heroWon();
        Inventory inv = game.getHero().getInventory();
        messages.prompt(won ? "Vittoria! Cosa vuoi fare?" : "Sei stato sconfitto... Cosa vuoi fare?");
        showMenu(sideBox, new MenuGrid(List.of(
                new MenuGrid.Entry(won ? "AVANTI" : "RIPROVA", true, won ? this::proceed : () -> {
                    game.startBattle();
                    startEncounter();
                }),
                new MenuGrid.Entry("POZIONE " + Game.POTION_PRICE + "G", inv.getGold() >= Game.POTION_PRICE, this::buyPotion),
                new MenuGrid.Entry("SALVA", true, this::save),
                new MenuGrid.Entry("TITOLO", true, onMenu)), 2, 0), null);
    }

    private void play(Action action) {
        if (!game.getHero().canUse(action)) {
            return;
        }
        showMessages(game.playTurn(action), () -> {
            if (game.getBattle().isOver()) {
                afterBattle();
            } else {
                showMainMenu();
            }
        });
    }

    /** A battaglia finita: prima si decide delle mosse nuove (una alla volta), poi il menu di fine battaglia. */
    private void afterBattle() {
        game.pendingSkill().ifPresentOrElse(this::showLearnMenu, this::showAfterBattleMenu);
    }

    /** Stile Pokémon: per imparare una mossa nuova se ne dimentica una, oppure si rinuncia. */
    private void showLearnMenu(Skill newSkill) {
        Hero hero = game.getHero();
        messages.prompt(hero.getName() + " vuole imparare " + newSkill.name() + ". Quale mossa dimentichi?");
        List<MenuGrid.Entry> entries = new ArrayList<>();
        for (Skill known : hero.getSkills()) {
            entries.add(new MenuGrid.Entry(known.label(), true,
                    () -> showMessages(game.learnPendingSkill(known), this::afterBattle), () -> showDetails(known)));
        }
        entries.add(new MenuGrid.Entry("NON IMPARARE " + newSkill.name().toUpperCase(), true,
                () -> showMessages(game.skipPendingSkill(), this::afterBattle), () -> showDetails(newSkill)));
        showMenu(mainBox, new MenuGrid(entries, 1, 0), null);
    }

    /** Dopo una vittoria: bivio, oppure dritti al boss. */
    private void proceed() {
        if (game.canChooseRoute()) {
            showRoutes();
        } else {
            game.takeRoute(Route.NORMAL);
            startEncounter();
        }
    }

    private void showRoutes() {
        List<MenuGrid.Entry> entries = new ArrayList<>();
        for (Route route : game.availableRoutes()) {
            entries.add(new MenuGrid.Entry(Labels.route(route), true, () -> takeRoute(route), () ->
                    sideBox.getChildren().setAll(new VBox(8,
                            detailTitle("Il sentiero si divide..."), detailText(Labels.routeDescription(route))))));
        }
        showMenu(mainBox, new MenuGrid(entries, 1, 0), this::showAfterBattleMenu);
    }

    private void takeRoute(Route route) {
        List<Message> log = game.takeRoute(route);
        if (log.isEmpty()) {
            startEncounter(); // il sentiero ha portato a una battaglia
        } else {
            refreshInfo(true);
            showMessages(log, this::proceed);
        }
    }

    /** STATO: statistiche e mosse dell'eroe. Non consuma il turno. */
    private void showStatus() {
        Hero hero = game.getHero();
        GridPane moves = new GridPane();
        moves.setHgap(14);
        moves.setVgap(3);
        int row = 0;
        // in ordine di livello: le mosse conosciute e quelle ancora da sbloccare (le dimenticate non si vedono più)
        for (Skill skill : hero.getHeroClass().skills()) {
            boolean known = hero.knows(skill);
            if (!known && skill.unlockLevel() <= hero.getLevel()) {
                continue;
            }
            Label name = new Label(known ? skill.name() : "Lv" + skill.unlockLevel() + "  ???");
            name.getStyleClass().add(known ? "status-move" : "status-locked");
            Label category = new Label(known ? skill.category() : "");
            category.getStyleClass().add("status-locked");
            Label pp = new Label(known ? "PP " + hero.getPp(skill) + "/" + skill.maxPp().orElse(0) : "");
            pp.getStyleClass().add("status-pp");
            moves.addRow(row++, name, category, pp);
        }
        mainBox.getChildren().setAll(moves);

        Label stats = new Label(String.format("%s  Lv%d%nHP %d/%d   EXP %d/%d%nATK %d   DEF %d",
                hero.getHeroClass().name().toUpperCase(), hero.getLevel(), hero.getHp(), hero.getMaxHp(),
                hero.getExp(), Hero.expToNextLevel(hero.getLevel()), hero.getAttack(), hero.getDefense()));
        stats.getStyleClass().add("status-stats");
        stats.setMinHeight(Region.USE_PREF_SIZE);
        MenuGrid back = new MenuGrid(List.of(new MenuGrid.Entry("INDIETRO", true, this::showMainMenu)), 1, 0);
        activeMenu = back;
        onBack = this::showMainMenu;
        setSideBoxVisible(true);
        sideBox.getChildren().setAll(new VBox(10, stats, back));
    }

    private void flee() {
        showMessages(List.of(new Message("Scappi e torni al menu principale.", Tone.INFO)), onMenu);
    }

    private void buyPotion() {
        Message result = game.buyPotion()
                ? new Message("Hai comprato una pozione. Ora ne hai " + game.getHero().getInventory().getPotions() + ".", Tone.REWARD)
                : new Message("Oro insufficiente.", Tone.DANGER);
        refreshInfo(true);
        showMessages(List.of(result), this::showAfterBattleMenu);
    }

    /** Il salvataggio parte in background: l'esito compare in alto quando la scrittura è finita. */
    private void save() {
        showSaveStatus("Salvataggio...", "save-busy");
        game.saveInBackground(saveFeedback);
        showMessages(List.of(new Message("Salvataggio in corso...", Tone.INFO)), this::showAfterBattleMenu);
    }

    // ---------------------------------------------------------------- messaggi e menu

    private void showMessages(List<Message> log, Runnable then) {
        activeMenu = null;
        onBack = null;
        setSideBoxVisible(false); // durante i messaggi il riquadro del testo occupa tutta la larghezza
        messages.play(log, this::animate, () -> {
            refreshInfo(true);
            then.run();
        });
    }

    private void showMenu(StackPane box, MenuGrid menu, Runnable back) {
        activeMenu = menu;
        onBack = back;
        setSideBoxVisible(true);
        box.getChildren().setAll(menu);
    }

    private void setSideBoxVisible(boolean visible) {
        sideBox.setVisible(visible);
        sideBox.setManaged(visible);
    }

    private void goBack() {
        if (onBack != null) {
            onBack.run();
        }
    }

    private void handleKey(KeyEvent e) {
        switch (e.getCode()) {
            case ENTER, SPACE, Z -> {
                if (activeMenu != null) {
                    activeMenu.pick();
                } else {
                    messages.advance();
                }
            }
            case ESCAPE, X, BACK_SPACE -> goBack();
            case UP, W -> moveCursor(0, -1);
            case DOWN, S -> moveCursor(0, 1);
            case LEFT, A -> moveCursor(-1, 0);
            case RIGHT, D -> moveCursor(1, 0);
            default -> {
                return;
            }
        }
        e.consume();
    }

    private void moveCursor(int dx, int dy) {
        if (activeMenu != null) {
            activeMenu.move(dx, dy);
        }
    }

    // ---------------------------------------------------------------- barre e animazioni

    private void refreshInfo(boolean animate) {
        Hero hero = game.getHero();
        Enemy enemy = game.getBattle().getEnemy();
        Inventory inv = hero.getInventory();

        // dopo una vittoria (o una fonte) Game è già passato allo stage verso cui si va
        int stage = game.getStage();
        boolean bossFight = enemy.isBoss() && !game.getBattle().isOver();
        stageLabel.setText(bossFight ? "STAGE " + stage + "   !! BOSS !!" : "STAGE " + stage);
        stageLabel.getStyleClass().setAll("label", "stage");
        if (bossFight) {
            stageLabel.getStyleClass().add("boss");
        }
        purse.setText(hero.getHeroClass().name() + "     Oro " + inv.getGold() + "     Pozioni " + inv.getPotions());

        heroInfo.showIdentity(hero);
        enemyInfo.showIdentity(enemy);
        shownHeroHp = hero.getHp();
        shownEnemyHp = enemy.getHp();
        heroInfo.hp().set(hero.getHp(), hero.getMaxHp(), animate);
        enemyInfo.hp().set(enemy.getHp(), enemy.getMaxHp(), animate);
        exp.set(hero.getExp(), Hero.expToNextLevel(hero.getLevel()), animate);
    }

    /** All'apertura di ogni messaggio: animazione della mossa e barre HP portate al valore del messaggio. */
    private void animate(Message message) {
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
        heroInfo.hp().set(heroHp, game.getHero().getMaxHp(), true);
        enemyInfo.hp().set(enemyHp, game.getBattle().getEnemy().getMaxHp(), true);
    }
}
