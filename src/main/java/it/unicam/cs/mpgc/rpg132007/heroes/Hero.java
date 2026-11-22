package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.core.Combatant;
import it.unicam.cs.mpgc.rpg132007.moves.Action;
import it.unicam.cs.mpgc.rpg132007.moves.Attack;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * L'eroe giocante: la classe scelta decide statistiche, aspetto e skill; cresce con l'EXP.
 * Quali mosse conosce e quanti PP gli restano lo gestisce il suo {@link Moveset}, oro e pozioni il suo {@link Inventory}.
 */
public class Hero extends Combatant {

    private static final int STARTING_POTIONS = 3;
    /** Per passare dal livello n al successivo servono EXP_PER_LEVEL * n EXP. */
    private static final int EXP_PER_LEVEL = 20;

    private final HeroClass heroClass;
    private final Inventory inventory;
    private final Moveset moveset;
    private final Action basicAttack = new Attack();
    private final Action potion;
    private int exp;

    public Hero(String name, HeroClass heroClass) {
        this(name, heroClass, 1, 0, new Inventory(0, STARTING_POTIONS));
    }

    public Hero(String name, HeroClass heroClass, int level, int exp, Inventory inventory) {
        super(name, level, heroClass.statsFor(level), heroClass.appearance());
        if (name.isBlank() || level < 1 || exp < 0) {
            throw new IllegalArgumentException("Eroe non valido: '" + name + "' Lv" + level + " EXP " + exp);
        }
        this.heroClass = heroClass;
        this.inventory = inventory;
        this.moveset = new Moveset(heroClass.skills(), level);
        this.potion = new DrinkPotion(inventory);
        this.exp = exp;
    }

    public static int expToNextLevel(int level) {
        return EXP_PER_LEVEL * level;
    }

    /** Aggiunge EXP e applica i level up. Ritorna livelli guadagnati e skill sbloccate (imparate o in attesa). */
    public LevelUp gainExp(int amount) {
        exp += amount;
        int gained = 0;
        List<Skill> learned = new ArrayList<>();
        List<Skill> waiting = new ArrayList<>();
        while (exp >= expToNextLevel(getLevel())) {
            exp -= expToNextLevel(getLevel());
            setLevel(getLevel() + 1);
            setStats(heroClass.statsFor(getLevel()));
            moveset.unlock(skillsUnlockedAt(getLevel()), learned, waiting);
            gained++;
        }
        return new LevelUp(gained, learned, waiting);
    }

    private List<Skill> skillsUnlockedAt(int level) {
        return heroClass.skills().stream().filter(skill -> skill.unlockLevel() == level).toList();
    }

    /** Mosse del menu LOTTA: l'attacco base più le skill conosciute. */
    public List<Action> getMoves() {
        List<Action> moves = new ArrayList<>();
        moves.add(basicAttack);
        moves.addAll(moveset.getSkills());
        return List.copyOf(moves);
    }

    /** Oggetti dello ZAINO usabili in battaglia. */
    public List<Action> getItems() {
        return List.of(potion);
    }

    /** PP sufficienti e azione disponibile (es. pozioni rimaste). */
    public boolean canUse(Action action) {
        return moveset.hasPp(action) && action.isUsableBy(this);
    }

    public Moveset getMoveset() {
        return moveset;
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
