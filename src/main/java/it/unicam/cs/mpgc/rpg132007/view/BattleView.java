package it.unicam.cs.mpgc.rpg132007.view;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.DrinkPotion;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.game.Game;
import it.unicam.cs.mpgc.rpg132007.game.Route;
import it.unicam.cs.mpgc.rpg132007.model.Element;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Hero;
import it.unicam.cs.mpgc.rpg132007.model.Inventory;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Schermata di battaglia in stile JRPG classico: nemico in alto a destra, eroe in basso a sinistra,
 * riquadro dei messaggi che scrive una lettera alla volta e menu a griglia (LOTTA / ZAINO / DIFESA / FUGA).
 * Il gioco calcola il turno tutto insieme; qui i messaggi vengono mostrati uno per volta e le barre
 * seguono gli HP che ogni messaggio porta con sé.
 */
class BattleView extends BorderPane {

    private static final Duration TYPE_SPEED = Duration.millis(14);

    private final Game game;
    private final Runnable onMenu;

    private final Label stageLabel = new Label();
    private final Label purse = new Label();
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
    private final Label text = new Label();
    private final Label nextArrow = new Label("▼");
    private final Timeline arrowBlink = new Timeline(
            new KeyFrame(Duration.millis(450), e -> nextArrow.setOpacity(nextArrow.getOpacity() > 0 ? 0 : 1)));

    private final Deque<Message> queue = new ArrayDeque<>();
    private Runnable afterMessages = () -> { };
    private Timeline typing;
    private String fullText = "";
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
        HBox top = new HBox(16, stageLabel, spacer, purse);
        top.setPadding(new Insets(12, 22, 0, 22));
        setTop(top);
        setCenter(arena());
        setBottom(bottomPanel());

        arrowBlink.setCycleCount(Timeline.INDEFINITE);
        setFocusTraversable(true);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        setOnMouseClicked(e -> requestFocus());
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                Platform.runLater(this::requestFocus);
            }
        });
        startEncounter();
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
        text.getStyleClass().add("message");
        text.setWrapText(true);
        text.setAlignment(Pos.TOP_LEFT);
        text.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        nextArrow.getStyleClass().add("next-arrow");
        StackPane.setAlignment(nextArrow, Pos.BOTTOM_RIGHT);

        mainBox.getStyleClass().add("textbox");
        mainBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(mainBox, Priority.ALWAYS);
        mainBox.setOnMouseClicked(e -> {
            if (activeMenu == null) {
                advance();
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
        slideIn(enemySprite, 420);
        slideIn(heroSprite, -420);

        List<Message> intro = new ArrayList<>();
        intro.add(enemy.isBoss()
                ? new Message("Un boss sbarra la strada: " + enemy.getName() + "!", Tone.DANGER)
                : new Message("Stage " + enemy.getLevel() + ": appare " + enemy.getName() + "!", Tone.INFO));
        intro.add(new Message("Avanti, " + hero.getName() + "!", Tone.HERO));
        showMessages(intro, this::showMainMenu);
    }

    private void showMainMenu() {
        Hero hero = game.getHero();
        setMainText("Cosa farà " + hero.getName() + "?");
        showMenu(sideBox, new MenuGrid(List.of(
                new MenuGrid.Entry("LOTTA", true, this::showMoves),
                new MenuGrid.Entry("ZAINO", true, this::showBag),
                new MenuGrid.Entry("STATO", true, this::showStatus),
                new MenuGrid.Entry("FUGA", true, this::flee)), 2, 0), null);
    }

    private void showMoves() {
        Hero hero = game.getHero();
        List<Action> moves = new ArrayList<>();
        moves.add(new Attack());
        moves.addAll(hero.getSkills());
        List<MenuGrid.Entry> entries = new ArrayList<>();
        for (Action move : moves) {
            entries.add(new MenuGrid.Entry(move.name(), hero.canUse(move), () -> {
                lastMoveCursor = activeMenu.cursor();
                play(move);
            }, () -> showDetails(move)));
        }
        showMenu(mainBox, new MenuGrid(entries, 2, lastMoveCursor), this::showMainMenu);
    }

    private void showBag() {
        Inventory inv = game.getHero().getInventory();
        DrinkPotion potion = new DrinkPotion(inv);
        MenuGrid.Entry entry = new MenuGrid.Entry("Pozione  x" + inv.getPotions(), game.getHero().canUse(potion),
                () -> play(potion), () -> showDetails(potion));
        showMenu(mainBox, new MenuGrid(List.of(entry), 1, 0), this::showMainMenu);
    }

    /** Riquadro a destra mentre si sceglie una mossa: PP rimasti, categoria e descrizione. */
    private void showDetails(Action move) {
        Hero hero = game.getHero();
        Label element = new Label();
        InfoBox.chip(element, move.element().name(), InfoBox.elementClass(move.element()));
        Label categoryText = new Label(move.category());
        categoryText.getStyleClass().add("detail-type");
        HBox category = new HBox(8, element, categoryText);
        category.setAlignment(Pos.CENTER_LEFT);
        String ppText = "PP   --";
        boolean available = hero.canUse(move);
        if (move instanceof Skill skill && skill.maxPp() != Skill.UNLIMITED) {
            ppText = String.format("PP   %d / %d", hero.getPp(skill), skill.maxPp());
        }
        Label ppLine = new Label(ppText);
        ppLine.getStyleClass().add(available ? "detail-ok" : "detail-ko");
        Label description = new Label(move.description());
        description.getStyleClass().add("detail-text");
        description.setWrapText(true);
        Label back = new Label("[X / Esc] indietro");
        back.getStyleClass().add("hint");
        back.setOnMouseClicked(e -> goBack());
        VBox details = new VBox(6, ppLine, category, description, back);
        sideBox.getChildren().setAll(details);
    }

    private void showAfterBattleMenu() {
        boolean won = game.getBattle().heroWon();
        Inventory inv = game.getHero().getInventory();
        setMainText(won ? "Vittoria! Cosa vuoi fare?" : "Sei stato sconfitto... Cosa vuoi fare?");
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
        Hero hero = game.getHero();
        if (!hero.canUse(action)) {
            return;
        }
        List<Message> log = game.playTurn(action);
        showMessages(log, () -> {
            if (game.getBattle().isOver()) {
                showAfterBattleMenu();
            } else {
                showMainMenu();
            }
        });
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
            entries.add(new MenuGrid.Entry(route.label(), true, () -> takeRoute(route), () -> {
                Label title = new Label("Il sentiero si divide...");
                title.getStyleClass().add("detail-type");
                Label description = new Label(route.description());
                description.getStyleClass().add("detail-text");
                description.setWrapText(true);
                sideBox.getChildren().setAll(new VBox(8, title, description));
            }));
        }
        showMenu(mainBox, new MenuGrid(entries, 1, 0), this::showAfterBattleMenu);
    }

    private void takeRoute(Route route) {
        List<Message> log = game.takeRoute(route);
        if (route == Route.SPRING) {
            refreshInfo(true);
            showMessages(log, this::proceed);
        } else {
            startEncounter();
        }
    }

    /** STATO: statistiche e mosse dell'eroe, più la tabella degli elementi. Non consuma il turno. */
    private void showStatus() {
        Hero hero = game.getHero();
        GridPane moves = new GridPane();
        moves.setHgap(14);
        moves.setVgap(3);
        int row = 0;
        for (Skill skill : hero.getHeroClass().skills()) {
            boolean unlocked = skill.unlockLevel() <= hero.getLevel();
            Label name = new Label(unlocked ? skill.name() : "Lv" + skill.unlockLevel() + "  ???");
            name.getStyleClass().add(unlocked ? "status-move" : "status-locked");
            Label element = new Label();
            InfoBox.chip(element, unlocked ? skill.element().name() : null, InfoBox.elementClass(skill.element()));
            Label pp = new Label(unlocked ? "PP " + hero.getPp(skill) + "/" + skill.maxPp() : "");
            pp.getStyleClass().add("status-pp");
            moves.addRow(row++, name, element, pp);
        }
        mainBox.getChildren().setAll(moves);

        Label stats = new Label(String.format("%s  Lv%d%nHP %d/%d   EXP %d/%d%nATK %d   DEF %d",
                hero.getHeroClass().name().toUpperCase(), hero.getLevel(), hero.getHp(), hero.getMaxHp(),
                hero.getExp(), Hero.expToNextLevel(hero.getLevel()), hero.getAttack(), hero.getDefense()));
        stats.getStyleClass().add("status-stats");
        stats.setMinHeight(Region.USE_PREF_SIZE);
        Label chart = new Label("FUOCO > NATURA > ACQUA > FUOCO\nLUCE <> OMBRA      x1.5 / x0.6");
        chart.getStyleClass().add("hint");
        chart.setMinHeight(Region.USE_PREF_SIZE);
        MenuGrid back = new MenuGrid(List.of(new MenuGrid.Entry("INDIETRO", true, this::showMainMenu)), 1, 0);
        activeMenu = back;
        onBack = this::showMainMenu;
        setSideBoxVisible(true);
        sideBox.getChildren().setAll(new VBox(6, stats, chart, back));
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

    private void save() {
        Message result;
        try {
            game.save();
            result = new Message("Partita salvata.", Tone.INFO);
        } catch (IOException e) {
            result = new Message("Errore nel salvataggio: " + e.getMessage(), Tone.DANGER);
        }
        showMessages(List.of(result), this::showAfterBattleMenu);
    }

    // ---------------------------------------------------------------- messaggi

    private void showMessages(List<Message> messages, Runnable then) {
        queue.addAll(messages);
        afterMessages = then;
        activeMenu = null;
        onBack = null;
        setSideBoxVisible(false); // durante i messaggi il riquadro del testo occupa tutta la larghezza
        nextMessage();
    }

    private void nextMessage() {
        Message message = queue.poll();
        if (message == null) {
            arrowBlink.stop();
            refreshInfo(true);
            afterMessages.run();
            return;
        }
        if (message.fx() != null) {
            playFx(message.fx());
        }
        if (message.hasHp()) {
            animateHp(message.heroHp(), message.enemyHp());
        }
        typeText(message);
    }

    private void typeText(Message message) {
        fullText = message.text();
        text.setText("");
        text.getStyleClass().setAll("label", "message", "msg-" + message.tone().name().toLowerCase());
        nextArrow.setOpacity(0);
        mainBox.getChildren().setAll(text, nextArrow);
        if (typing != null) {
            typing.stop();
        }
        typing = new Timeline(new KeyFrame(TYPE_SPEED, e -> {
            text.setText(fullText.substring(0, text.getText().length() + 1));
            if (text.getText().length() == fullText.length()) {
                typingDone();
            }
        }));
        typing.setCycleCount(fullText.length());
        typing.play();
    }

    private void typingDone() {
        typing.stop();
        text.setText(fullText);
        arrowBlink.play();
    }

    /** Invio / click: completa subito il testo in corso, oppure passa al messaggio successivo. */
    private void advance() {
        if (typing != null && typing.getStatus() == Timeline.Status.RUNNING) {
            typingDone();
        } else {
            arrowBlink.stop();
            nextMessage();
        }
    }

    private void setMainText(String prompt) {
        text.getStyleClass().setAll("label", "message");
        text.setText(prompt);
        mainBox.getChildren().setAll(text);
    }

    private void showMenu(StackPane box, MenuGrid menu, Runnable back) {
        activeMenu = menu;
        onBack = back;
        setSideBoxVisible(true);
        if (box == mainBox) {
            mainBox.getChildren().setAll(menu);
        } else {
            sideBox.getChildren().setAll(menu);
        }
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
                    advance();
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

        // dopo una vittoria (o una fonte) mostra già lo stage verso cui si va
        int stage = game.getBattle().heroWon() ? game.getStage() : enemy.getLevel();
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

    private void animateHp(int heroHp, int enemyHp) {
        Hero hero = game.getHero();
        Enemy enemy = game.getBattle().getEnemy();
        if (enemyHp < shownEnemyHp) {
            hit(enemySprite, enemyHp == 0);
        }
        if (heroHp < shownHeroHp) {
            hit(heroSprite, heroHp == 0);
        }
        shownHeroHp = heroHp;
        shownEnemyHp = enemyHp;
        heroInfo.hp().set(heroHp, hero.getMaxHp(), true);
        enemyInfo.hp().set(enemyHp, enemy.getMaxHp(), true);
    }

    /** Lampeggio + scossone; se il colpo è fatale lo sprite scende e svanisce. */
    private static void hit(Node sprite, boolean fatal) {
        FadeTransition blink = new FadeTransition(Duration.millis(70), sprite);
        blink.setFromValue(1);
        blink.setToValue(0.15);
        blink.setCycleCount(6);
        blink.setAutoReverse(true);
        TranslateTransition shake = new TranslateTransition(Duration.millis(45), sprite);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        ParallelTransition anim = new ParallelTransition(blink, shake);
        if (fatal) {
            anim.setOnFinished(e -> {
                TranslateTransition drop = new TranslateTransition(Duration.millis(450), sprite);
                drop.setToY(80);
                FadeTransition fade = new FadeTransition(Duration.millis(450), sprite);
                fade.setToValue(0);
                new ParallelTransition(drop, fade).play();
            });
        }
        anim.play();
    }

    /** Fotogrammi ASCII delle animazioni, uno per elemento, più le scintille per cure e potenziamenti. */
    private static final Map<Element, String[]> FX_FRAMES = Map.of(
            Element.NEUTRO, new String[]{"    /\n   / \n  /  ", "    /  /\n   /  / \n  /  /  ", " \\   /  /\n  \\ /  / \n   X  /  \n  / \\/   "},
            Element.FUOCO, new String[]{"   (   \n  ) )  \n (___) ", "  ) (  \n ( ) ) \n(_)(_)", " ( ) ( \n) ( ) )\n(_)(_)(_)"},
            Element.ACQUA, new String[]{"  o   \n      \n      ", " o  o \n  o   \n o    ", "o  o o\n o  o \n~~~~~~"},
            Element.NATURA, new String[]{"   *   \n       ", "  * & *\n   &   ", " & * & \n* & * &\n & * & "},
            Element.LUCE, new String[]{"   +   ", " \\ | / \n-- * --\n / | \\ ", "  .*.  \n* *** *\n  '*'  "},
            Element.OMBRA, new String[]{"  .:.  ", " :@@@: \n.@@@@@.\n :@@@: ", ".:   :.\n  :@:  \n':   :'"});
    private static final String[] SUPPORT_FRAMES = {"   +   ", "  + +  \n   +   ", " +   + \n  + +  \n   +   ", "+     +\n +   + \n       "};

    /** Animazione della mossa: chi attacca fa uno slancio, sul bersaglio compaiono i fotogrammi dell'elemento. */
    private void playFx(Message.Fx fx) {
        StackPane target = fx.onEnemySide() ? enemyStage : heroStage;
        if (!fx.support()) {
            Node attacker = fx.fromHero() ? heroSprite : enemySprite;
            TranslateTransition lunge = new TranslateTransition(Duration.millis(110), attacker);
            lunge.setByX(fx.fromHero() ? 30 : -30);
            lunge.setAutoReverse(true);
            lunge.setCycleCount(2);
            lunge.play();
        }
        String[] frames = fx.support() ? SUPPORT_FRAMES : FX_FRAMES.get(fx.element());
        Label overlay = new Label();
        overlay.setMouseTransparent(true);
        overlay.getStyleClass().addAll("fx", fx.support() ? "fx-support" : "fx-" + fx.element().name().toLowerCase());
        StackPane.setAlignment(overlay, Pos.CENTER);
        target.getChildren().add(overlay);
        Timeline anim = new Timeline();
        for (int i = 0; i < frames.length; i++) {
            String frame = frames[i];
            anim.getKeyFrames().add(new KeyFrame(Duration.millis(90 + 110 * i), e -> overlay.setText(frame)));
        }
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(90 + 110 * frames.length),
                e -> target.getChildren().remove(overlay)));
        anim.play();
    }

    private static void slideIn(Node sprite, double fromX) {
        sprite.setOpacity(1);
        sprite.setTranslateY(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), sprite);
        slide.setFromX(fromX);
        slide.setToX(0);
        slide.play();
    }
}
