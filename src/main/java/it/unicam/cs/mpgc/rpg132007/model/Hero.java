package it.unicam.cs.mpgc.rpg132007.model;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** L'eroe giocante: la classe scelta decide statistiche, aspetto e skill; cresce con l'EXP. */
public class Hero extends Combatant {

    private static final int STARTING_POTIONS = 3;

    private final HeroClass heroClass;
    private final Inventory inventory;
    private int exp;
    /** PP rimasti per nome della mossa: durano da una battaglia all'altra. */
    private final Map<String, Integer> pp = new HashMap<>();

    public Hero(String name, HeroClass heroClass) {
        this(name, heroClass, 1, 0, new Inventory(0, STARTING_POTIONS));
    }

    public Hero(String name, HeroClass heroClass, int level, int exp, Inventory inventory) {
        super(name, level, heroClass.statsFor(level), heroClass.appearance(), Element.NEUTRO);
        if (name.isBlank() || level < 1 || exp < 0) {
            throw new IllegalArgumentException("Eroe non valido: '" + name + "' Lv" + level + " EXP " + exp);
        }
        this.heroClass = heroClass;
        this.inventory = inventory;
        this.exp = exp;
    }

    public static int expToNextLevel(int level) {
        return 20 * level;
    }

    /** Aggiunge EXP e applica i level up. Ritorna quanti livelli sono stati guadagnati. */
    public int gainExp(int amount) {
        exp += amount;
        int gained = 0;
        while (exp >= expToNextLevel(getLevel())) {
            exp -= expToNextLevel(getLevel());
            setLevel(getLevel() + 1);
            setStats(heroClass.statsFor(getLevel()));
            gained++;
        }
        return gained;
    }

    public List<Skill> getSkills() {
        return heroClass.skills().stream().filter(skill -> skill.unlockLevel() <= getLevel()).toList();
    }

    /** Energia sufficiente e azione disponibile (es. pozioni rimaste). */
    public boolean canUse(Action action) {
        boolean hasPp = !(action instanceof Skill skill) || skill.maxPp() == Skill.UNLIMITED || getPp(skill) > 0;
        return hasPp && action.isUsableBy(this);
    }

    /** PP rimasti; una mossa mai usata li ha tutti. */
    public int getPp(Skill skill) {
        return pp.getOrDefault(skill.name(), skill.maxPp());
    }

    /** Scala un PP se l'azione è una mossa a PP limitati. */
    public void spendPp(Action action) {
        if (action instanceof Skill skill && skill.maxPp() != Skill.UNLIMITED) {
            pp.put(skill.name(), Math.max(0, getPp(skill) - 1));
        }
    }

    /** Ricarica tutti i PP (dopo un boss, dopo una sconfitta, al caricamento). */
    public void restorePp() {
        pp.clear();
    }

    /** PP delle sole mosse già usate (le altre sono al massimo): è ciò che finisce nel salvataggio. */
    public Map<String, Integer> getUsedPp() {
        return Map.copyOf(pp);
    }

    /** Ripristina i PP letti da un salvataggio; i nomi di mosse che non esistono più vengono ignorati. */
    public void loadPp(Map<String, Integer> saved) {
        pp.clear();
        for (Skill skill : heroClass.skills()) {
            Integer left = saved.get(skill.name());
            if (left != null) {
                pp.put(skill.name(), Math.clamp(left, 0, skill.maxPp()));
            }
        }
    }

    public HeroClass getHeroClass() {
        return heroClass;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getExp() {
        return exp;
    }

}
