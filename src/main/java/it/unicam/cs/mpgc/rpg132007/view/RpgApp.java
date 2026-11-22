package it.unicam.cs.mpgc.rpg132007.view;

import it.unicam.cs.mpgc.rpg132007.game.Game;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClassCatalog;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.persistence.JsonSaveRepository;
import it.unicam.cs.mpgc.rpg132007.world.StageEnemyFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Avvio dell'applicazione e navigazione tra le schermate (titolo, scelta eroe, battaglia).
 * È anche l'unico punto in cui si scelgono le implementazioni concrete (nemici, classi, salvataggi).
 */
public class RpgApp extends Application {

    private HeroClassCatalog heroClasses;
    private Game game;
    private Scene scene;
    private String monoFont;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) {
        // primo font monospace disponibile, perché gli sprite ASCII restino allineati (i font vanno letti dopo l'avvio di JavaFX)
        monoFont = Stream.of("Consolas", "DejaVu Sans Mono", "Menlo", "Courier New")
                .filter(Font.getFamilies()::contains)
                .findFirst()
                .orElse("Monospaced");
        heroClasses = new HeroClasses();
        game = new Game(new StageEnemyFactory(), heroClasses, JsonSaveRepository.inUserHome(), new Random());
        // la finestra si adatta allo schermo (anche con lo zoom di Windows al 125-150%)
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        scene = new Scene(new Region(), Math.min(1100, screen.getWidth() * 0.95), Math.min(800, screen.getHeight() * 0.92));
        scene.getStylesheets().add(RpgApp.class.getResource("style.css").toExternalForm());
        showTitle("");
        window.setTitle("Asciipon");
        window.setMinWidth(900);
        window.setMinHeight(600);
        window.setScene(scene);
        window.show();
    }

    private void showTitle(String message) {
        show(new TitleView(message, this::showHeroSelect, this::loadGame, Platform::exit));
    }

    private void showHeroSelect() {
        show(new HeroSelectView(heroClasses.all(), (name, heroClass) -> {
            game.newGame(name, heroClass);
            showBattle();
        }, () -> showTitle("")));
    }

    private void showBattle() {
        show(new BattleView(game, () -> showTitle("")));
    }

    private void loadGame() {
        try {
            if (game.load()) {
                showBattle();
            } else {
                showTitle("Nessun salvataggio trovato.");
            }
        } catch (IOException e) {
            showTitle("Impossibile caricare: " + e.getMessage());
        }
    }

    private void show(Parent root) {
        root.setStyle("-fx-font-family: '" + monoFont + "';");
        scene.setRoot(root);
    }
}
