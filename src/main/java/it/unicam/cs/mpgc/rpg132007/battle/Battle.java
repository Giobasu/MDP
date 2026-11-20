package it.unicam.cs.mpgc.rpg132007.battle;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.battle.Message.Fx;
import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Enemy;
import it.unicam.cs.mpgc.rpg132007.model.Hero;
import it.unicam.cs.mpgc.rpg132007.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Un combattimento a turni eroe contro nemico. Non sa nulla di grafica né di salvataggi.
 * L'unica parte casuale (la paralisi) usa un generatore passato da fuori, così i test sono ripetibili.
 */
public class Battle {

    private final Hero hero;
    private final Enemy enemy;
    private final RandomGenerator random;

    public Battle(Hero hero, Enemy enemy) {
        this(hero, enemy, new Random());
    }

    public Battle(Hero hero, Enemy enemy, RandomGenerator random) {
        this.hero = hero;
        this.enemy = enemy;
        this.random = random;
    }

    /** Un turno completo: prima l'eroe, poi il nemico se è ancora vivo, infine gli stati alterati. */
    public List<Message> playTurn(Action heroAction) {
        if (isOver()) {
            throw new IllegalStateException("La battaglia è già finita");
        }
        if (!hero.canUse(heroAction)) {
            return List.of(message("Non puoi usare " + heroAction.name() + " adesso.", Tone.INFO));
        }
        List<Message> log = new ArrayList<>();
        if (canAct(hero, Tone.HERO, log)) {
            hero.spendPp(heroAction); // chi dorme o è paralizzato non consuma PP, come nei JRPG classici
            perform(hero, enemy, heroAction, Tone.HERO, log);
        }
        if (enemy.isAlive() && canAct(enemy, Tone.ENEMY, log)) {
            // chooseAction viene valutato prima di perform, quindi l'IA vede ancora la guardia del turno prima
            perform(enemy, hero, enemy.chooseAction(hero), Tone.ENEMY, log);
        }
        endOfTurn(hero, log);
        endOfTurn(enemy, log);
        return log;
    }

    /** Inizio azione: il sonno fa saltare il turno finché dura, la paralisi a volte. */
    private boolean canAct(Combatant who, Tone tone, List<Message> log) {
        if (who.getStatus() == Status.SONNO) {
            // salta questo turno; se era l'ultimo turno di sonno si sveglia subito dopo e il prossimo agisce
            log.add(message(who.getName() + " dorme profondamente...", tone));
            if (who.countDownStatus()) {
                log.add(message(who.getName() + " " + Status.SONNO.curedText() + "!", tone));
            }
            return false;
        }
        if (who.getStatus() == Status.PARALISI && random.nextInt(100) < Status.PARALYSIS_SKIP_CHANCE) {
            log.add(message(who.getName() + " è paralizzato! Non riesce a muoversi.", tone));
            return false;
        }
        return true;
    }

    /** La guardia dura fino al turno successivo di chi l'ha alzata. */
    private void perform(Combatant user, Combatant target, Action action, Tone tone, List<Message> log) {
        user.setDefending(false);
        String[] lines = action.execute(user, target).split("\n");
        boolean fromHero = user == hero;
        boolean onEnemySide = action.targetsEnemy() == fromHero;
        Fx fx = new Fx(onEnemySide, fromHero, action.element(), !action.targetsEnemy());
        for (int i = 0; i < lines.length; i++) {
            log.add(new Message(lines[i], tone, hero.getHp(), enemy.getHp(), i == 0 ? fx : null));
        }
    }

    /** Fine turno: veleno e bruciatura fanno danni, poi ogni stato (tranne il sonno) consuma un turno. */
    private void endOfTurn(Combatant who, List<Message> log) {
        Status status = who.getStatus();
        if (isOver() || status == null || status == Status.SONNO) {
            return;
        }
        if (status.damagePercent() > 0) {
            int damage = who.loseHpPercent(status.damagePercent());
            String cause = status == Status.VELENO ? " soffre per il veleno: " : " soffre per la bruciatura: ";
            log.add(message(who.getName() + cause + damage + " danni.", Tone.DANGER));
            if (isOver()) {
                return;
            }
        }
        if (who.countDownStatus()) {
            log.add(message(who.getName() + " " + status.curedText() + ".", Tone.INFO));
        }
    }

    private Message message(String text, Tone tone) {
        return new Message(text, tone, hero.getHp(), enemy.getHp());
    }

    public boolean isOver() {
        return !hero.isAlive() || !enemy.isAlive();
    }

    public boolean heroWon() {
        return hero.isAlive() && !enemy.isAlive();
    }

    public Hero getHero() {
        return hero;
    }

    public Enemy getEnemy() {
        return enemy;
    }
}
