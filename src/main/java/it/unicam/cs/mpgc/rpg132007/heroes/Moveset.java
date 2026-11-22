package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.moves.Action;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Le mosse di un eroe: quali conosce (al massimo {@value #MAX_SKILLS}), quali aspettano un posto libero
 * e quanti PP restano a ciascuna. Livelli ed EXP restano all'{@link Hero}, che qui dice solo cosa si è sbloccato.
 */
public final class Moveset {

    public static final int MAX_SKILLS = 4;

    private final List<Skill> skills = new ArrayList<>();
    /** Skill sbloccate quando non c'era posto: in ordine di sblocco. */
    private final Deque<Skill> pending = new ArrayDeque<>();
    /** PP rimasti per nome della mossa: durano da una battaglia all'altra. Le mosse mai usate non ci sono (PP al massimo). */
    private final Map<String, Integer> pp = new HashMap<>();

    /** Conosce le prime skill già sbloccate al livello dato, fino al limite. */
    Moveset(List<Skill> classSkills, int level) {
        classSkills.stream()
                .filter(skill -> skill.unlockLevel() <= level)
                .limit(MAX_SKILLS)
                .forEach(skills::add);
    }

    /** Skill appena sbloccate: imparate subito se c'è posto, altrimenti in attesa. Aggiunge ciascuna a learned o a waiting. */
    void unlock(List<Skill> unlocked, List<Skill> learned, List<Skill> waiting) {
        for (Skill skill : unlocked) {
            if (skills.size() < MAX_SKILLS) {
                skills.add(skill);
                learned.add(skill);
            } else {
                pending.add(skill);
                waiting.add(skill);
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

    /** Nomi delle skill conosciute. */
    public List<String> getSkillNames() {
        return skills.stream().map(Skill::name).toList();
    }

    /** La prossima skill che si vuole imparare ma per cui non c'è posto, se c'è. */
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

    /** true se l'azione non ha PP limitati o gliene resta almeno uno. */
    public boolean hasPp(Action action) {
        return action.maxPp().isEmpty() || getPp(action) > 0;
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

    /** PP delle sole mosse già usate (le altre sono al massimo). */
    public Map<String, Integer> getUsedPp() {
        return Map.copyOf(pp);
    }

    /**
     * Sostituisce le skill conosciute e i PP rimasti (es. letti da un salvataggio).
     * Con known vuota si tengono le skill attuali; i PP vengono riportati tra 0 e il massimo della mossa.
     */
    public void restore(List<Skill> known, Map<Skill, Integer> ppLeft) {
        if (!known.isEmpty()) {
            skills.clear();
            known.stream().distinct().limit(MAX_SKILLS).forEach(skills::add);
        }
        pp.clear();
        ppLeft.forEach((skill, left) -> skill.maxPp().ifPresent(max -> pp.put(skill.name(), Math.clamp(left, 0, max))));
    }
}
