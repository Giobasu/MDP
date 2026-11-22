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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Schermata di battaglia in stile JRPG classico: campo di battaglia in alto, riquadro dei messaggi e menu a griglia
 * (LOTTA / ZAINO / STATO / FUGA) in basso. Questa classe guida il flusso (menu, scelte, tastiera); cosa si vede
 * lo disegnano {@link BattleArena}, {@link BattleHeader}, {@link HeroPanels} e {@link MessageBox}.
 * Le mosse disponibili le decide il modello.
 */
class BattleView extends BorderPane {

    private final Game game;
    private final Runnable onMenu;

    private final BattleHeader header = new BattleHeader();
    private final BattleArena arena = new BattleArena();
    /** Riquadro grande in basso a sinistra: testo dei messaggi oppure griglia delle mosse. */
    private final StackPane mainBox = new StackPane();
    /** Riquadro piccolo in basso a destra: menu principale oppure dettagli della mossa. */
    private final StackPane sideBox = new StackPane();
    private final MessageBox messages = new MessageBox(mainBox);
    /** I due riquadri con il bordo ASCII attorno. */
    private final AsciiFrame mainFrame = new AsciiFrame(mainBox);
    private final AsciiFrame sideFrame = new AsciiFrame(sideBox);

    private MenuGrid activeMenu;
    private Runnable onBack;
    private int lastMoveCursor;

    /**
     * Esito dei salvataggi in background, mostrato in alto. Viene chiamato dal thread di salvataggio:
     * JavaFX permette di toccare la grafica solo dal suo thread, quindi si passa da Platform.runLater.
     */
    private final SaveListener saveFeedback = new SaveListener() {
        @Override
        public void saved() {
            Platform.runLater(() -> header.showSaveStatus("Partita salvata", "save-ok"));
        }

        @Override
        public void failed(IOException error) {
            Platform.runLater(() -> header.showSaveStatus("Salvataggio non riuscito: " + error.getMessage(), "save-ko"));
        }
    };

    BattleView(Game game, Runnable onMenu) {
        this.game = game;
        this.onMenu = onMenu;
        getStyleClass().add("battle");
        setTop(header);
        setCenter(arena);
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

    private HBox bottomPanel() {
        mainFrame.getStyleClass().add("textbox");
        sideFrame.getStyleClass().add("textbox");
        mainBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(mainFrame, Priority.ALWAYS);
        mainFrame.setOnMouseClicked(e -> {
            if (activeMenu == null) {
                messages.advance();
            }
        });
        sideBox.setAlignment(Pos.CENTER_LEFT);
        sideBox.setMinWidth(300);
        sideBox.setPrefWidth(300);

        HBox bottom = new HBox(6, mainFrame, sideFrame);
        bottom.setPadding(new Insets(0, 18, 16, 18));
        bottom.setPrefHeight(190);
        bottom.setMinHeight(170);
        return bottom;
    }

    // ---------------------------------------------------------------- flusso della battaglia

    private void startEncounter() {
        Hero hero = game.getHero();
        Enemy enemy = game.getBattle().getEnemy();
        arena.enter(hero, enemy);
        refreshInfo(false);

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

    private void showDetails(Action move) {
        sideBox.getChildren().setAll(HeroPanels.moveDetails(game.getHero(), move, this::goBack));
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
        for (Skill known : hero.getMoveset().getSkills()) {
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
                    sideBox.getChildren().setAll(new VBox(8, HeroPanels.detailTitle("Il sentiero si divide..."),
                            HeroPanels.detailText(Labels.routeDescription(route))))));
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
        mainBox.getChildren().setAll(HeroPanels.moveList(hero));
        MenuGrid back = new MenuGrid(List.of(new MenuGrid.Entry("INDIETRO", true, this::showMainMenu)), 1, 0);
        activeMenu = back;
        onBack = this::showMainMenu;
        setSideBoxVisible(true);
        sideBox.getChildren().setAll(new VBox(10, HeroPanels.stats(hero), back));
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
        header.showSaveStatus("Salvataggio...", "save-busy");
        game.saveInBackground(saveFeedback);
        showMessages(List.of(new Message("Salvataggio in corso...", Tone.INFO)), this::showAfterBattleMenu);
    }

    /** Barra in alto e campo di battaglia allineati al modello. */
    private void refreshInfo(boolean animate) {
        // dopo una vittoria (o una fonte) Game è già passato allo stage verso cui si va
        boolean bossFight = game.getBattle().getEnemy().isBoss() && !game.getBattle().isOver();
        header.update(game.getStage(), bossFight, game.getHero());
        arena.refresh(animate);
    }

    // ---------------------------------------------------------------- messaggi e menu

    private void showMessages(List<Message> log, Runnable then) {
        activeMenu = null;
        onBack = null;
        setSideBoxVisible(false); // durante i messaggi il riquadro del testo occupa tutta la larghezza
        messages.play(log, arena::animate, () -> {
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
        sideFrame.setVisible(visible);
        sideFrame.setManaged(visible);
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
}
