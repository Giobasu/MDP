package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.battle.Battle;
import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Hero;
import it.unicam.cs.mpgc.rpg132007.model.Inventory;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveData;
import it.unicam.cs.mpgc.rpg132007.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg132007.world.EnemyFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Lo stato della partita: eroe, stage corrente e battaglia in corso.
 * È l'unico punto d'ingresso per l'interfaccia grafica, che quindi si può sostituire senza toccare il gioco.
 * Prima di usarla serve {@link #newGame} o {@link #load}.
 */
public class Game {

    public static final int POTION_PRICE = 30;

    private final EnemyFactory enemies;
    private final SaveRepository saves;
    private final RandomGenerator random;
    private Hero hero;
    private int stage;
    private boolean elite;
    private boolean justRested;
    private Battle battle;

    public Game(EnemyFactory enemies, SaveRepository saves) {
        this(enemies, saves, new Random());
    }

    /** Con un generatore fissato (seed) le partite sono ripetibili: serve ai test. */
    public Game(EnemyFactory enemies, SaveRepository saves, RandomGenerator random) {
        this.enemies = enemies;
        this.saves = saves;
        this.random = random;
    }

    public void newGame(String heroName, HeroClass heroClass) {
        hero = new Hero(heroName, heroClass);
        stage = 1;
        elite = false;
        startBattle();
    }

    /** (Ri)comincia la battaglia dello stage corrente, sul sentiero scelto per ultimo (riprova dopo una sconfitta). */
    public void startBattle() {
        hero.restore();
        battle = new Battle(hero, elite ? enemies.createElite(stage) : enemies.create(stage), random);
    }

    /** Dopo una vittoria: se il prossimo stage è di un boss non c'è scelta di sentiero. */
    public boolean canChooseRoute() {
        return !enemies.isBossStage(stage);
    }

    /** La fonte non si può prendere due volte di fila, altrimenti si salterebbero stage senza combattere. */
    public List<Route> availableRoutes() {
        return justRested ? List.of(Route.NORMAL, Route.ELITE) : List.of(Route.values());
    }

    /**
     * Prende un sentiero verso lo stage corrente. NORMAL ed ELITE iniziano la battaglia;
     * SPRING ricarica i PP, avanza di uno stage senza combattere e ritorna i messaggi da mostrare.
     */
    public List<Message> takeRoute(Route route) {
        if (!availableRoutes().contains(route)) {
            throw new IllegalStateException("Sentiero non disponibile: " + route);
        }
        justRested = route == Route.SPRING;
        if (route == Route.SPRING) {
            hero.restorePp();
            stage++;
            return List.of(
                    new Message("Trovi una fonte limpida e ti riposi.", Tone.INFO),
                    new Message("I PP di tutte le mosse sono stati ricaricati!", Tone.REWARD));
        }
        elite = route == Route.ELITE;
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
        elite = false;
        if (enemy.isBoss()) {
            hero.restorePp();
            log.add(new Message("Il boss è caduto: i PP di tutte le mosse sono stati ricaricati!", Tone.REWARD));
            log.add(autoSave());
        }
        return log;
    }

    private Message autoSave() {
        try {
            save();
            return new Message("Salvataggio automatico completato.", Tone.INFO);
        } catch (IOException e) {
            return new Message("Salvataggio automatico non riuscito: " + e.getMessage(), Tone.DANGER);
        }
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
            for (Skill skill : hero.getSkills()) {
                if (skill.unlockLevel() > oldLevel) {
                    log.add(new Message("Nuova abilità sbloccata: " + skill.name() + "!", Tone.REWARD));
                }
            }
        }
        return log;
    }

    public void save() throws IOException {
        Inventory inv = hero.getInventory();
        saves.save(new SaveData(hero.getName(), hero.getHeroClass().id(), hero.getLevel(), hero.getExp(),
                stage, inv.getGold(), inv.getPotions(), hero.getUsedPp()));
    }

    /** Ritorna false se non c'è nessun salvataggio. La battaglia in corso riparte da capo. */
    public boolean load() throws IOException {
        Optional<SaveData> found = saves.load();
        if (found.isEmpty()) {
            return false;
        }
        SaveData data = found.get();
        HeroClass heroClass = HeroClasses.byId(data.heroClass())
                .orElseThrow(() -> new IOException("Classe eroe sconosciuta nel salvataggio: " + data.heroClass()));
        hero = new Hero(data.name(), heroClass, data.level(), data.exp(), new Inventory(data.gold(), data.potions()));
        hero.loadPp(data.usedPp());
        stage = data.stage();
        elite = false;
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
