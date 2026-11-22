package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.battle.Battle;
import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.enemies.EnemyFactory;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.AutoSaveStarted;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRefill;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRestored;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillForgotten;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillLearned;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillSkipped;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SpringFound;
import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClassCatalog;
import it.unicam.cs.mpgc.rpg132007.moves.Action;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;
import it.unicam.cs.mpgc.rpg132007.save.SaveListener;
import it.unicam.cs.mpgc.rpg132007.save.SaveRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Lo stato della partita: eroe, stage corrente e battaglia in corso.
 * È l'unico punto d'ingresso per l'interfaccia grafica, che quindi si può sostituire senza toccare il gioco.
 * Tutte le dipendenze (nemici, classi, salvataggi, testi, caso) arrivano da fuori.
 * Decide come avanza la partita; le ricompense le calcola {@link Rewards}, i salvataggi li gestisce {@link SaveSlot}.
 * Non scrive testo: produce {@link GameEvent} e li fa raccontare dal narratore.
 * Prima di usarla serve {@link #newGame} o {@link #load}.
 */
public class Game {

    public static final int POTION_PRICE = 30;

    private final EnemyFactory enemies;
    private final SaveSlot saves;
    private final Rewards rewards;
    private final GameNarrator narrator;
    private final RandomGenerator random;
    private Hero hero;
    private int stage;
    /** Decide sia il nemico (élite o no, anche quando si riprova) sia se la fonte è disponibile. */
    private Route lastRoute;
    private Battle battle;

    /** Con un generatore fissato (seed) le partite sono ripetibili: serve ai test. */
    public Game(EnemyFactory enemies, HeroClassCatalog heroClasses, SaveRepository saves, GameNarrator narrator,
                RandomGenerator random) {
        this.enemies = enemies;
        this.saves = new SaveSlot(saves, heroClasses);
        this.rewards = new Rewards(narrator);
        this.narrator = narrator;
        this.random = random;
    }

    public void newGame(String heroName, HeroClass heroClass) {
        hero = new Hero(heroName, heroClass);
        stage = 1;
        lastRoute = Route.NORMAL;
        startBattle();
    }

    /** (Ri)comincia la battaglia dello stage corrente, sul sentiero scelto per ultimo (riprova dopo una sconfitta). */
    public void startBattle() {
        hero.restore();
        Enemy enemy = lastRoute == Route.ELITE ? enemies.createElite(stage) : enemies.create(stage);
        battle = new Battle(hero, enemy, random, narrator);
    }

    /** Dopo una vittoria: se il prossimo stage è di un boss non c'è scelta di sentiero. */
    public boolean canChooseRoute() {
        return !enemies.isBossStage(stage);
    }

    /** La fonte non si può prendere due volte di fila, altrimenti si salterebbero stage senza combattere. */
    public List<Route> availableRoutes() {
        return lastRoute == Route.SPRING ? List.of(Route.NORMAL, Route.ELITE) : List.of(Route.values());
    }

    /**
     * Prende un sentiero verso lo stage corrente. NORMAL ed ELITE iniziano la battaglia;
     * SPRING ricarica i PP, avanza di uno stage senza combattere e ritorna i messaggi da mostrare.
     */
    public List<Message> takeRoute(Route route) {
        if (!availableRoutes().contains(route)) {
            throw new IllegalStateException("Sentiero non disponibile: " + route);
        }
        lastRoute = route;
        if (route == Route.SPRING) {
            hero.getMoveset().restorePp();
            stage++;
            return List.of(say(new SpringFound(), Tone.INFO), say(new PpRestored(PpRefill.SPRING), Tone.REWARD));
        }
        startBattle();
        return List.of();
    }

    public List<Message> playTurn(Action action) {
        List<Message> log = new ArrayList<>(battle.playTurn(action));
        if (battle.isOver()) {
            log.addAll(battle.heroWon() ? victory() : rewards.defeat(hero, battle.getEnemy(), stage));
        }
        return log;
    }

    /** Si passa allo stage successivo; dopo un boss la partita si salva da sola. */
    private List<Message> victory() {
        stage++;
        Enemy enemy = battle.getEnemy();
        List<Message> log = new ArrayList<>(rewards.victory(hero, enemy));
        if (enemy.isBoss()) {
            saves.autoSave(hero, stage);
            log.add(say(new AutoSaveStarted(), Tone.INFO));
        }
        return log;
    }

    /** Tra una battaglia e l'altra. Ritorna false se l'oro non basta. */
    public boolean buyPotion() {
        if (!hero.getInventory().spendGold(POTION_PRICE)) {
            return false;
        }
        hero.getInventory().addPotion();
        return true;
    }

    /** La skill che l'eroe vuole imparare ma per cui deve dimenticarne un'altra (vedi {@link #learnPendingSkill}). */
    public Optional<Skill> pendingSkill() {
        return hero.getMoveset().getPendingSkill();
    }

    /** Impara la skill in attesa dimenticando forgotten. */
    public List<Message> learnPendingSkill(Skill forgotten) {
        Skill learned = pendingSkill().orElseThrow(() -> new IllegalStateException("Nessuna skill in attesa"));
        hero.getMoveset().learnPendingSkill(forgotten);
        return List.of(
                say(new SkillForgotten(hero.getName(), forgotten.name()), Tone.INFO),
                say(new SkillLearned(hero.getName(), learned.name()), Tone.REWARD));
    }

    /** Rinuncia alla skill in attesa. */
    public List<Message> skipPendingSkill() {
        Skill skipped = pendingSkill().orElseThrow(() -> new IllegalStateException("Nessuna skill in attesa"));
        hero.getMoveset().skipPendingSkill();
        return List.of(say(new SkillSkipped(hero.getName(), skipped.name()), Tone.INFO));
    }

    private Message say(GameEvent event, Tone tone) {
        return new Message(narrator.describe(event), tone);
    }

    /** Salva subito, nel thread di chi chiama. */
    public void save() throws IOException {
        saves.saveNow(hero, stage);
    }

    /** Salva in un thread separato e avvisa il listener a scrittura finita (vedi {@link SaveSlot#saveInBackground}). */
    public void saveInBackground(SaveListener listener) {
        saves.saveInBackground(hero, stage, listener);
    }

    /** Il listener del salvataggio automatico dopo ogni boss. */
    public void setAutoSaveListener(SaveListener listener) {
        saves.setAutoSaveListener(listener);
    }

    /** Ritorna false se non c'è nessun salvataggio. La battaglia in corso riparte da capo. */
    public boolean load() throws IOException {
        Optional<SaveSlot.Loaded> found = saves.load();
        if (found.isEmpty()) {
            return false;
        }
        hero = found.get().hero();
        stage = found.get().stage();
        lastRoute = Route.NORMAL;
        startBattle();
        return true;
    }

    public Hero getHero() {
        return hero;
    }

    public int getStage() {
        return stage;
    }

    public Battle getBattle() {
        return battle;
    }
}
