package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.Stats;

import java.util.List;

/**
 * Una classe giocabile. Le statistiche crescono linearmente: base + crescita * livello.
 * L'id è usato nei salvataggi, quindi non va cambiato.
 */
public record HeroClass(String id, String name, String description, Appearance appearance,
                        Stats base, Stats growth, List<Skill> skills) {

    public HeroClass {
        skills = List.copyOf(skills);
    }

    public Stats statsFor(int level) {
        return new Stats(
                base.maxHp() + growth.maxHp() * level,
                base.attack() + growth.attack() * level,
                base.defense() + growth.defense() * level);
    }
}
