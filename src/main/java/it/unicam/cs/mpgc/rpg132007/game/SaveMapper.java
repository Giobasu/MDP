package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClassCatalog;
import it.unicam.cs.mpgc.rpg132007.heroes.Inventory;
import it.unicam.cs.mpgc.rpg132007.heroes.Moveset;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;
import it.unicam.cs.mpgc.rpg132007.save.SaveData;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converte la partita in {@link SaveData} e viceversa. Sa cosa si salva, non dove né in che formato.
 * Nel salvataggio le mosse sono scritte per nome: qui si ritrovano tra quelle della classe.
 */
final class SaveMapper {

    private final HeroClassCatalog heroClasses;

    SaveMapper(HeroClassCatalog heroClasses) {
        this.heroClasses = heroClasses;
    }

    SaveData toSaveData(Hero hero, int stage) {
        Inventory inv = hero.getInventory();
        Moveset moves = hero.getMoveset();
        return new SaveData(hero.getName(), hero.getHeroClass().id(), hero.getLevel(), hero.getExp(),
                stage, inv.getGold(), inv.getPotions(), moves.getUsedPp(), moves.getSkillNames());
    }

    Hero toHero(SaveData data) throws IOException {
        HeroClass heroClass = heroClasses.byId(data.heroClass())
                .orElseThrow(() -> new IOException("Classe eroe sconosciuta nel salvataggio: " + data.heroClass()));
        Hero hero = new Hero(data.name(), heroClass, data.level(), data.exp(), new Inventory(data.gold(), data.potions()));
        hero.getMoveset().restore(knownSkills(heroClass, data.skills(), data.level()), ppLeft(heroClass, data.usedPp()));
        return hero;
    }

    /**
     * Le skill conosciute nel salvataggio. Si ignorano i nomi sconosciuti o non ancora sbloccati;
     * se non ne resta nessuna (es. tutte rinominate) il Moveset tiene quelle di base.
     */
    private static List<Skill> knownSkills(HeroClass heroClass, List<String> names, int level) {
        return names.stream()
                .flatMap(name -> find(heroClass, name).stream())
                .filter(skill -> skill.unlockLevel() <= level)
                .toList();
    }

    /** I PP rimasti; i nomi di mosse che non esistono più vengono ignorati. */
    private static Map<Skill, Integer> ppLeft(HeroClass heroClass, Map<String, Integer> saved) {
        Map<Skill, Integer> left = new HashMap<>();
        saved.forEach((name, pp) -> find(heroClass, name).ifPresent(skill -> left.put(skill, pp)));
        return left;
    }

    private static Optional<Skill> find(HeroClass heroClass, String name) {
        return heroClass.skills().stream().filter(s -> s.name().equals(name)).findFirst();
    }
}
