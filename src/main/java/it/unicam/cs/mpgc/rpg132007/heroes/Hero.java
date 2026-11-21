package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.action.Action;
import it.unicam.cs.mpgc.rpg132007.action.Attack;
import it.unicam.cs.mpgc.rpg132007.action.DrinkPotion;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Inventory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * L'eroe giocante: la classe scelta decide statistiche, aspetto e skill; cresce con l'EXP.
 * Conosce al massimo {@value #MAX_SKILLS} skill: quelle sbloccate oltre il limite restano in attesa
 * finché il giocatore non ne dimentica una per farle posto (o rinuncia a impararle).
 */
public class Hero extends Combatant {

    public static final int MAX_SKILLS = 4;
    private static final int STARTING_POTIONS = 3;

    private final HeroClass heroClass;
    private final Inventory inventory;
    private final Action basicAttack = new Attack();
    private final Action potion;
    private int exp;
    /** PP rimasti per nome della mossa: durano da una battaglia all'altra. */
    private final Map<String, Integer> pp = new HashMap<>();
    private final List<Skill> skills = new ArrayList<>();
    /** Skill sbloccate quando non c'era posto: in ordine di sblocco. */
    private final Deque<Skill> pending = new ArrayDeque<>();

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
        this.potion = new DrinkPotion(inventory);
        this.exp = exp;
        // un eroe creato a un certo livello conosce le prime skill sbloccate, fino al limite
        heroClass.skills().stream()
                .filter(skill -> skill.unlockLevel() <= level)
                .limit(MAX_SKILLS)
                .forEach(skills::add);
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
            unlockSkillsOf(getLevel());
            gained++;
        }
        return gained;
    }

    /** Le skill che si sbloccano a questo livello: imparate subito se c'è posto, altrimenti in attesa. */
    private void unlockSkillsOf(int level) {
        for (Skill skill : heroClass.skills()) {
            if (skill.unlockLevel() == level) {
                if (skills.size() < MAX_SKILLS) {
                    skills.add(skill);
                } else {
                    pending.add(skill);
                }
            }
        }
    }

    /** Skill conosciute, nell'ordine del menu. */
    public List<Skill> getSkills() {
        return List.copyOf(skills);
    }

    public boolean knows(Skill skill) {
        return skills.contains(skill);
    }

    /** La prossima skill che vuole imparare ma per cui non c'è posto, se c'è. */
    public Optional<Skill> getPendingSkill() {
        return Optional.ofNullable(pending.peek());
    }

    /** Impara la skill in attesa al posto di una conosciuta, che si dimentica (insieme ai suoi PP). */
    public void learnPendingSkill(Skill forgotten) {
        int slot = skills.indexOf(forgotten);
        if (pending.isEmpty() || slot < 0) {
            throw new IllegalStateException("Nessuna skill in attesa o skill da dimenticare non conosciuta");
        }
        skills.set(slot, pending.poll());
        pp.remove(forgotten.name());
    }

    /** Rinuncia alla skill in attesa: non si potrà più imparare. */
    public void skipPendingSkill() {
        if (pending.poll() == null) {
            throw new IllegalStateException("Nessuna skill in attesa");
        }
    }

    /** Mosse del menu LOTTA: l'attacco base più le skill conosciute. */
    public List<Action> getMoves() {
        List<Action> moves = new ArrayList<>();
        moves.add(basicAttack);
        moves.addAll(getSkills());
        return List.copyOf(moves);
    }

    /** Oggetti dello ZAINO usabili in battaglia. */
    public List<Action> getItems() {
        return List.of(potion);
    }

    /** PP sufficienti e azione disponibile (es. pozioni rimaste). */
    public boolean canUse(Action action) {
        boolean hasPp = action.maxPp().isEmpty() || getPp(action) > 0;
        return hasPp && action.isUsableBy(this);
    }

    /** PP rimasti; una mossa mai usata li ha tutti. 0 per le mosse senza PP. */
    public int getPp(Action action) {
        return pp.getOrDefault(action.name(), action.maxPp().orElse(0));
    }

    /** Scala un PP se l'azione ha PP limitati. */
    public void spendPp(Action action) {
        if (action.maxPp().isPresent()) {
            pp.put(action.name(), Math.max(0, getPp(action) - 1));
        }
    }

    /** Ricarica tutti i PP (dopo un boss, dopo una sconfitta, alla fonte). */
    public void restorePp() {
        pp.clear();
    }

    /** PP delle sole mosse già usate (le altre sono al massimo): è ciò che finisce nel salvataggio. */
    public Map<String, Integer> getUsedPp() {
        return Map.copyOf(pp);
    }

    /** Nomi delle skill conosciute: è ciò che finisce nel salvataggio. */
    public List<String> getSkillNames() {
        return skills.stream().map(Skill::name).toList();
    }

    /**
     * Ripristina le skill conosciute lette da un salvataggio. Si ignorano i nomi sconosciuti o non ancora sbloccati;
     * se non ne resta nessuno (salvataggi vecchi) si tengono quelle di base.
     */
    public void loadSkills(List<String> names) {
        List<Skill> loaded = names.stream()
                .flatMap(name -> heroClass.skills().stream().filter(s -> s.name().equals(name)))
                .filter(skill -> skill.unlockLevel() <= getLevel())
                .distinct()
                .limit(MAX_SKILLS)
                .toList();
        if (!loaded.isEmpty()) {
            skills.clear();
            skills.addAll(loaded);
        }
    }

    /** Ripristina i PP letti da un salvataggio; i nomi di mosse che non esistono più vengono ignorati. */
    public void loadPp(Map<String, Integer> saved) {
        pp.clear();
        for (Skill skill : heroClass.skills()) {
            Integer left = saved.get(skill.name());
            OptionalInt max = skill.maxPp();
            if (left != null && max.isPresent()) {
                pp.put(skill.name(), Math.clamp(left, 0, max.getAsInt()));
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
