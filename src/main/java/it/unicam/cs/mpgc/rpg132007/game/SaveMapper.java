package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClass;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClassCatalog;
import it.unicam.cs.mpgc.rpg132007.heroes.Inventory;
import it.unicam.cs.mpgc.rpg132007.save.SaveData;

import java.io.IOException;

/** Converte la partita in {@link SaveData} e viceversa. Sa cosa si salva, non dove né in che formato. */
final class SaveMapper {

    private final HeroClassCatalog heroClasses;

    SaveMapper(HeroClassCatalog heroClasses) {
        this.heroClasses = heroClasses;
    }

    SaveData toSaveData(Hero hero, int stage) {
        Inventory inv = hero.getInventory();
        return new SaveData(hero.getName(), hero.getHeroClass().id(), hero.getLevel(), hero.getExp(),
                stage, inv.getGold(), inv.getPotions(), hero.getUsedPp(), hero.getSkillNames());
    }

    Hero toHero(SaveData data) throws IOException {
        HeroClass heroClass = heroClasses.byId(data.heroClass())
                .orElseThrow(() -> new IOException("Classe eroe sconosciuta nel salvataggio: " + data.heroClass()));
        Hero hero = new Hero(data.name(), heroClass, data.level(), data.exp(), new Inventory(data.gold(), data.potions()));
        hero.loadSkills(data.skills());
        hero.loadPp(data.usedPp());
        return hero;
    }
}
