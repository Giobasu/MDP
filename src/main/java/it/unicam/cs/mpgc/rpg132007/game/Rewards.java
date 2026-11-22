package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.battle.Message.Tone;
import it.unicam.cs.mpgc.rpg132007.battle.Message;
import it.unicam.cs.mpgc.rpg132007.enemies.Enemy;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.Defeated;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.EnemyDefeated;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.ExpGained;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.GoldFound;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.LevelReached;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRefill;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.PpRestored;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillLearned;
import it.unicam.cs.mpgc.rpg132007.game.GameEvent.SkillWaiting;
import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.LevelUp;
import it.unicam.cs.mpgc.rpg132007.heroes.Moveset;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * Cosa succede all'eroe a fine battaglia: oro, EXP, livelli, mosse sbloccate e ricarica dei PP.
 * Applica le ricompense e ritorna i messaggi che le raccontano. Stage e salvataggi restano a {@link Game}.
 */
final class Rewards {

    /** Percentuale dell'EXP del nemico che si tiene anche perdendo. */
    static final int DEFEAT_EXP_PERCENT = 50;

    private final GameNarrator narrator;

    Rewards(GameNarrator narrator) {
        this.narrator = narrator;
    }

    /** Vittoria: oro ed EXP del nemico; un boss ricarica anche i PP. */
    List<Message> victory(Hero hero, Enemy enemy) {
        hero.getInventory().addGold(enemy.getReward().gold());
        List<Message> log = new ArrayList<>();
        log.add(say(new EnemyDefeated(enemy.getName()), Tone.REWARD));
        log.add(say(new GoldFound(enemy.getReward().gold()), Tone.REWARD));
        log.addAll(gainExp(hero, enemy.getReward().exp()));
        if (enemy.isBoss()) {
            hero.getMoveset().restorePp();
            log.add(say(new PpRestored(PpRefill.BOSS), Tone.REWARD));
        }
        return log;
    }

    /** Sconfitta: si tiene parte dell'EXP e si ricaricano i PP, così non si resta mai bloccati. */
    List<Message> defeat(Hero hero, Enemy enemy, int stage) {
        hero.getMoveset().restorePp();
        List<Message> log = new ArrayList<>();
        log.add(say(new Defeated(stage), Tone.DANGER));
        log.add(say(new PpRestored(PpRefill.DEFEAT), Tone.INFO));
        log.addAll(gainExp(hero, enemy.getReward().exp() * DEFEAT_EXP_PERCENT / 100));
        return log;
    }

    private List<Message> gainExp(Hero hero, int exp) {
        List<Message> log = new ArrayList<>();
        log.add(say(new ExpGained(exp), Tone.REWARD));
        LevelUp levelUp = hero.gainExp(exp);
        if (levelUp.levelsGained() > 0) {
            log.add(say(new LevelReached(hero.getLevel()), Tone.REWARD));
        }
        for (Skill skill : levelUp.learned()) {
            log.add(say(new SkillLearned(hero.getName(), skill.name()), Tone.REWARD));
        }
        for (Skill skill : levelUp.waiting()) {
            log.add(say(new SkillWaiting(hero.getName(), skill.name(), Moveset.MAX_SKILLS), Tone.INFO));
        }
        return log;
    }

    private Message say(GameEvent event, Tone tone) {
        return new Message(narrator.describe(event), tone);
    }
}
