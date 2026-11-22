package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.MoveContext;
import it.unicam.cs.mpgc.rpg132007.battle.Message.MoveUse;
import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.MoveUnavailable;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusCured;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.StatusDamage;
import it.unicam.cs.mpgc.rpg132007.model.BattleEvent.TurnSkipped;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Un combattimento a turni eroe contro nemico. Non sa nulla di grafica né di salvataggi,
 * e nemmeno di testi: le frasi le scrive il {@link Narrator}.
 * Le parti casuali (colpi critici, stati che fanno saltare il turno) usano un generatore passato da fuori,
 * così i test sono ripetibili.
 */
public class Battle {

    private final Hero hero;
    private final Enemy enemy;
    private final RandomGenerator random;
    private final Narrator narrator;

    /** Probabilità (%) che una mossa sia un colpo critico. */
    public static final int CRITICAL_CHANCE = 6;

    public Battle(Hero hero, Enemy enemy, RandomGenerator random, Narrator narrator) {
        this.hero = hero;
        this.enemy = enemy;
        this.random = random;
        this.narrator = narrator;
    }

    /** Un turno completo: prima l'eroe, poi il nemico se è ancora vivo, infine gli stati alterati. */
    public List<Message> playTurn(Action heroAction) {
        if (isOver()) {
            throw new IllegalStateException("La battaglia è già finita");
        }
        if (!hero.canUse(heroAction)) {
            return List.of(message(new MoveUnavailable(heroAction.name()), Tone.INFO));
        }
        List<Message> log = new ArrayList<>();
        if (canAct(hero, Tone.HERO, log)) {
            hero.spendPp(heroAction); // chi salta il turno non consuma PP, come nei JRPG classici
            perform(hero, enemy, heroAction, Tone.HERO, log);
        }
        if (enemy.isAlive() && canAct(enemy, Tone.ENEMY, log)) {
            perform(enemy, hero, enemy.chooseAction(), Tone.ENEMY, log);
        }
        endOfTurn(hero, log);
        endOfTurn(enemy, log);
        return log;
    }

    /** Inizio azione: è lo stato alterato a decidere se il turno salta (sempre col sonno, a volte con la paralisi). */
    private boolean canAct(Combatant who, Tone tone, List<Message> log) {
        Status status = who.getStatus();
        if (status == null || !status.skipsTurn(random)) {
            return true;
        }
        log.add(message(new TurnSkipped(who.getName(), status), tone));
        // se era l'ultimo turno (es. di sonno) guarisce subito e il prossimo turno agisce
        if (status.countsDownOnSkip() && who.countDownStatus()) {
            log.add(message(new StatusCured(who.getName(), status), tone));
        }
        return false;
    }

    private void perform(Combatant user, Combatant target, Action action, Tone tone, List<Message> log) {
        MoveContext context = new MoveContext(random.nextInt(100) < CRITICAL_CHANCE);
        List<BattleEvent> events = action.execute(user, target, context);
        MoveUse move = new MoveUse(user == hero, action.targetsEnemy());
        for (int i = 0; i < events.size(); i++) {
            log.add(new Message(narrator.describe(events.get(i)), tone, hero.getHp(), enemy.getHp(), i == 0 ? move : null));
        }
    }

    /** Fine turno: gli stati che fanno danni li fanno, poi quelli che si consumano a fine turno perdono un turno. */
    private void endOfTurn(Combatant who, List<Message> log) {
        Status status = who.getStatus();
        if (isOver() || status == null || status.countsDownOnSkip()) {
            return;
        }
        if (status.damagePercent() > 0) {
            int damage = who.loseHpPercent(status.damagePercent());
            log.add(message(new StatusDamage(who.getName(), status, damage), Tone.DANGER));
            if (isOver()) {
                return;
            }
        }
        if (who.countDownStatus()) {
            log.add(message(new StatusCured(who.getName(), status), Tone.INFO));
        }
    }

    private Message message(BattleEvent event, Tone tone) {
        return new Message(narrator.describe(event), tone, hero.getHp(), enemy.getHp());
    }

    public boolean isOver() {
        return !hero.isAlive() || !enemy.isAlive();
    }

    public boolean heroWon() {
        return hero.isAlive() && !enemy.isAlive();
    }

    public Enemy getEnemy() {
        return enemy;
    }
}
