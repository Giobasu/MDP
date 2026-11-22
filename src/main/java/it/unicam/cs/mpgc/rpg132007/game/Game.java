package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.battle.Battle;
import it.unicam.cs.mpgc.rpg132007.battle.ItalianNarrator;
import it.unicam.cs.mpgc.rpg132007.battle.Narrator;
import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClassCatalog;
import it.unicam.cs.mpgc.rpg132007.persistence.BackgroundSaver;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveData;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveListener;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg132007.world.EnemyFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Lo stato della partita: eroe, stage corrente e battaglia in corso.
 * È l'unico punto d'ingresso per l'interfaccia grafica, che quindi si può sostituire senza toccare il gioco.
 * Tutte le dipendenze (nemici, classi, salvataggi, caso) arrivano da fuori.
 * Prima di usarla serve {@link #newGame} o {@link #load}.
 */
public class Game {

    public static final int POTION_PRICE = 30;

    private final EnemyFactory enemies;
    private final SaveRepository saves;
    private final SaveMapper saveMapper;
    private final BackgroundSaver saver;
    private final RandomGenerator random;
    private final Narrator narrator = new ItalianNarrator();
    private Hero hero;
    private int stage;
    /** Decide sia il nemico (élite o no, anche quando si riprova) sia se la fonte è disponibile. */
    private Route lastRoute;
    private Battle battle;
    /** Chi vuole sapere com'è andato il salvataggio automatico dopo un boss (di solito la view). */
    private SaveListener autoSaveListener = SaveListener.IGNORE;

    /** Con un generatore fissato (seed) le partite sono ripetibili: serve ai test. */
    public Game(EnemyFactory enemies, HeroClassCatalog heroClasses, SaveRepository saves, RandomGenerator random) {
        this.enemies = enemies;
        this.saves = saves;
        this.saveMapper = new SaveMapper(heroClasses);
        this.saver = new BackgroundSaver(saves);
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
            hero.restorePp();
            stage++;
            return List.of(
                    new Message("Trovi una fonte limpida e ti riposi.", Tone.INFO),
                    new Message("I PP di tutte le mosse sono stati ricaricati!", Tone.REWARD));
        }
        startBattle();
        return List.of();
    }

    public List<Message> playTurn(Action action) {
        List<Message> log = new ArrayList<>(battle.playTurn(action));
        if (battle.isOver()) {
            log.addAll(battle.heroWon() ? victory() : defeat());
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

    private List<Message> victory() {
        stage++;
        Enemy enemy = battle.getEnemy();
        hero.getInventory().addGold(enemy.getReward().gold());
        List<Message> log = new ArrayList<>();
        log.add(new Message(enemy.getName() + " è stato sconfitto!", Tone.REWARD));
        log.add(new Message("Trovi " + enemy.getReward().gold() + " monete d'oro.", Tone.REWARD));
        log.addAll(gainExp(enemy.getReward().exp()));
        if (enemy.isBoss()) {
            hero.restorePp();
            log.add(new Message("Il boss è caduto: i PP di tutte le mosse sono stati ricaricati!", Tone.REWARD));
            saveInBackground(autoSaveListener);
            log.add(new Message("Salvataggio automatico in corso...", Tone.INFO));
        }
        return log;
    }

    /** Se perdi tieni metà EXP, ricarichi i PP e riprovi lo stesso stage: così non resti mai bloccato. */
    private List<Message> defeat() {
        hero.restorePp();
        List<Message> log = new ArrayList<>();
        log.add(new Message("Sei stato sconfitto... riprova lo stage " + stage + ".", Tone.DANGER));
        log.add(new Message("Ti riposi: i PP delle mosse sono stati ricaricati.", Tone.INFO));
        log.addAll(gainExp(battle.getEnemy().getReward().exp() / 2));
        return log;
    }

    private List<Message> gainExp(int exp) {
        List<Message> log = new ArrayList<>();
        log.add(new Message("Ottieni " + exp + " EXP.", Tone.REWARD));
        int oldLevel = hero.getLevel();
        if (hero.gainExp(exp) > 0) {
            log.add(new Message("LEVEL UP! Ora sei al livello " + hero.getLevel() + ".", Tone.REWARD));
            for (Skill skill : hero.getHeroClass().skills()) {
                if (skill.unlockLevel() > oldLevel && skill.unlockLevel() <= hero.getLevel()) {
                    log.add(hero.knows(skill)
                            ? new Message(hero.getName() + " impara " + skill.name() + "!", Tone.REWARD)
                            : new Message(hero.getName() + " vuole imparare " + skill.name() + ", ma conosce già "
                                    + Hero.MAX_SKILLS + " mosse.", Tone.INFO));
                }
            }
        }
        return log;
    }

    /** La skill che l'eroe vuole imparare ma per cui deve dimenticarne un'altra (vedi {@link #learnPendingSkill}). */
    public Optional<Skill> pendingSkill() {
        return hero.getPendingSkill();
    }

    /** Impara la skill in attesa dimenticando forgotten. */
    public List<Message> learnPendingSkill(Skill forgotten) {
        Skill learned = hero.getPendingSkill().orElseThrow(() -> new IllegalStateException("Nessuna skill in attesa"));
        hero.learnPendingSkill(forgotten);
        return List.of(
                new Message("1, 2 e... puf! " + hero.getName() + " dimentica " + forgotten.name() + "...", Tone.INFO),
                new Message("...e impara " + learned.name() + "!", Tone.REWARD));
    }

    /** Rinuncia alla skill in attesa. */
    public List<Message> skipPendingSkill() {
        Skill skipped = hero.getPendingSkill().orElseThrow(() -> new IllegalStateException("Nessuna skill in attesa"));
        hero.skipPendingSkill();
        return List.of(new Message(hero.getName() + " non impara " + skipped.name() + ".", Tone.INFO));
    }

    /** Salva subito, nel thread di chi chiama. */
    public void save() throws IOException {
        saver.saveNow(snapshot());
    }

    /**
     * Salva in un thread separato e avvisa il listener a scrittura finita.
     * La "fotografia" della partita si scatta qui, nel thread di chi chiama: il thread di salvataggio
     * lavora solo su dati immutabili, quindi la partita può continuare intanto senza race condition.
     */
    public void saveInBackground(SaveListener listener) {
        saver.saveInBackground(snapshot(), listener);
    }

    /** Il listener del salvataggio automatico dopo ogni boss. */
    public void setAutoSaveListener(SaveListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener null: usare SaveListener.IGNORE");
        }
        autoSaveListener = listener;
    }

    private SaveData snapshot() {
        return saveMapper.toSaveData(hero, stage);
    }

    /** Ritorna false se non c'è nessun salvataggio. La battaglia in corso riparte da capo. */
    public boolean load() throws IOException {
        Optional<SaveData> found = saves.load();
        if (found.isEmpty()) {
            return false;
        }
        hero = saveMapper.toHero(found.get());
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
