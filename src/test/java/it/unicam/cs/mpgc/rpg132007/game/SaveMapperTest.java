package it.unicam.cs.mpgc.rpg132007.game;

import it.unicam.cs.mpgc.rpg132007.heroes.Hero;
import it.unicam.cs.mpgc.rpg132007.heroes.HeroClasses;
import it.unicam.cs.mpgc.rpg132007.heroes.Moveset;
import it.unicam.cs.mpgc.rpg132007.moves.Skill;
import it.unicam.cs.mpgc.rpg132007.save.SaveData;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SaveMapperTest {

    private final SaveMapper mapper = new SaveMapper(new HeroClasses());

    private Hero load(List<String> skills, Map<String, Integer> usedPp) throws IOException {
        return mapper.toHero(new SaveData("Test", "warrior", 9, 0, 1, 0, 0, usedPp, skills));
    }

    @Test
    void loadedSkillsIgnoreUnknownOrLockedNames() throws IOException {
        Hero hero = load(List.of("Spada Divina", "Inesistente", "Squarcio Infuocato", "Fendente"), Map.of());
        assertEquals(List.of("Spada Divina", "Fendente"), hero.getMoveset().getSkillNames(), "Squarcio Infuocato è del Lv12");

        Hero old = load(List.of(), Map.of());
        assertEquals(Moveset.MAX_SKILLS, old.getMoveset().getSkills().size(), "senza nomi validi si tengono le prime skill");
    }

    @Test
    void loadedPpAreClampedAndUnknownNamesIgnored() throws IOException {
        Hero hero = load(List.of("Fendente"), Map.of("Fendente", 999, "Inesistente", 3));
        Skill fendente = hero.getMoveset().getSkills().getFirst();
        assertEquals(fendente.maxPp().getAsInt(), hero.getMoveset().getPp(fendente));
        assertEquals(Map.of("Fendente", fendente.maxPp().getAsInt()), hero.getMoveset().getUsedPp());
    }

    @Test
    void unknownHeroClassIsAnIoError() {
        SaveData data = new SaveData("Test", "mago", 1, 0, 1, 0, 0, Map.of(), List.of());
        assertThrows(IOException.class, () -> mapper.toHero(data));
    }
}
