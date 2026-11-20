package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.action.DamageSkill;
import it.unicam.cs.mpgc.rpg132007.action.Effect;
import it.unicam.cs.mpgc.rpg132007.action.StatusSkill;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.Element;
import it.unicam.cs.mpgc.rpg132007.model.Stats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static it.unicam.cs.mpgc.rpg132007.model.Stat.ATTACK;
import static it.unicam.cs.mpgc.rpg132007.model.Stat.DEFENSE;
import static it.unicam.cs.mpgc.rpg132007.model.Element.*;
import static it.unicam.cs.mpgc.rpg132007.model.Status.*;

/**
 * Le classi giocabili. Per aggiungerne una basta aggiungere una voce a questa lista
 * (e, se serve, uno sprite .txt accanto a questa classe nelle risorse).
 */
public final class HeroClasses {

    public static final List<HeroClass> ALL = List.of(
            new HeroClass("warrior", "Guerriero",
                    "Scudo e spada: incassa i colpi, stordisce e chiude con fendenti di luce e fuoco.",
                    new Appearance(sprite("warrior.txt"), "#e8c170"),
                    new Stats(64, 10, 6), new Stats(11, 4, 1),
                    List.of(
                            damage("Fendente", 1, 15, NEUTRO, 160, 1, false,
                                    "Un colpo di spada potente.", Effect.NONE),
                            self("Muro di Scudi", 2, 8, NEUTRO, "STATO",
                                    "Alza molto la tua DIFESA.", Effect.raise(DEFENSE, 2)),
                            damage("Colpo di Scudo", 3, 10, NEUTRO, 90, 1, false,
                                    "Stordisce: il nemico resta PARALIZZATO per 3 turni.", Effect.inflict(PARALISI, 3)),
                            self("Secondo Respiro", 5, 5, LUCE, "CURA",
                                    "Recupera il 35% degli HP massimi.", Effect.heal(35)),
                            self("Grido di Guerra", 7, 8, NEUTRO, "STATO",
                                    "Alza molto il tuo ATTACCO.", Effect.raise(ATTACK, 2)),
                            damage("Spada Sacra", 9, 8, LUCE, 140, 1, true,
                                    "Luce che ignora la difesa e la abbassa.", Effect.lower(DEFENSE, 1)),
                            damage("Colpo Devastante", 12, 5, FUOCO, 260, 1, false,
                                    "Lama infuocata: BRUCIA il nemico.", Effect.inflict(BRUCIATURA, 3)))),
            new HeroClass("archer", "Arciere",
                    "Fragile ma micidiale: frecce multiple, veleno, sonno e tiri che bucano le armature.",
                    new Appearance(sprite("archer.txt"), "#9be37b"),
                    new Stats(50, 11, 3), new Stats(12, 4, 1),
                    List.of(
                            damage("Doppio Tiro", 1, 15, NEUTRO, 75, 2, false,
                                    "Due frecce in rapida successione.", Effect.NONE),
                            damage("Freccia Avvelenata", 2, 10, NATURA, 70, 1, false,
                                    "AVVELENA il nemico per 4 turni.", Effect.inflict(VELENO, 4)),
                            foe("Dardo Soporifero", 3, 5, NATURA, "STATO",
                                    "ADDORMENTA il nemico per 2 turni. Non funziona sui boss.", Effect.inflict(SONNO, 2)),
                            damage("Freccia di Luce", 5, 8, LUCE, 160, 1, true,
                                    "Ignora completamente la difesa.", Effect.NONE),
                            self("Erbe Mediche", 7, 5, NATURA, "CURA",
                                    "Recupera il 30% degli HP massimi.", Effect.heal(30)),
                            damage("Tiro Spezzacorazza", 9, 8, NEUTRO, 90, 1, false,
                                    "Abbassa molto la DIFESA nemica.", Effect.lower(DEFENSE, 2)),
                            damage("Pioggia di Frecce", 12, 5, ACQUA, 60, 5, false,
                                    "Cinque frecce gelide dal cielo.", Effect.NONE))),
            new HeroClass("lancer", "Lanciere",
                    "Equilibrato e tenace: affondi che ignorano la difesa, ferite d'ombra e lance infuocate.",
                    new Appearance(sprite("lancer.txt"), "#7fb2ff"),
                    new Stats(52, 9, 4), new Stats(12, 3, 1),
                    List.of(
                            damage("Affondo", 1, 15, NEUTRO, 100, 1, true,
                                    "Un colpo di punta che ignora la difesa.", Effect.NONE),
                            damage("Squarcio", 2, 10, OMBRA, 80, 1, false,
                                    "Ferita profonda: il nemico sanguina (VELENO) per 4 turni.", Effect.inflict(VELENO, 4)),
                            self("Concentrazione", 3, 8, NEUTRO, "STATO",
                                    "Alza il tuo ATTACCO e la tua DIFESA.",
                                    Effect.raise(ATTACK, 1).and(Effect.raise(DEFENSE, 1))),
                            damage("Lancia Vampira", 5, 8, OMBRA, 110, 1, false,
                                    "Assorbe metà dei danni inflitti.", Effect.drain(50)),
                            damage("Giavellotto Infuocato", 7, 8, FUOCO, 150, 1, false,
                                    "Un lancio in fiamme che BRUCIA il nemico.", Effect.inflict(BRUCIATURA, 3)),
                            damage("Infilzata", 9, 8, NEUTRO, 60, 3, true,
                                    "Tre affondi rapidi che ignorano la difesa.", Effect.NONE),
                            damage("Lancia Celeste", 12, 5, LUCE, 200, 1, true,
                                    "Colpo supremo: ignora la difesa e assorbe HP.", Effect.drain(30)))));

    private HeroClasses() {
    }

    public static Optional<HeroClass> byId(String id) {
        return ALL.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    private static DamageSkill damage(String name, int level, int pp, Element element, int power, int hits,
                                      boolean piercing, String description, Effect effect) {
        return new DamageSkill(name, level, pp, element, power, hits, piercing, description, effect);
    }

    /** Mossa di stato su se stessi (cure, potenziamenti). */
    private static StatusSkill self(String name, int level, int pp, Element element, String category,
                                    String description, Effect effect) {
        return new StatusSkill(name, level, pp, element, category, false, description, effect);
    }

    /** Mossa di stato sull'avversario (stati alterati, indebolimenti). */
    private static StatusSkill foe(String name, int level, int pp, Element element, String category,
                                   String description, Effect effect) {
        return new StatusSkill(name, level, pp, element, category, true, description, effect);
    }

    /** Legge uno sprite dalle risorse, togliendo righe vuote in cima e in fondo. */
    private static List<String> sprite(String file) {
        try (InputStream in = HeroClasses.class.getResourceAsStream(file)) {
            if (in == null) {
                throw new IllegalStateException("Sprite mancante: " + file);
            }
            List<String> lines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().map(String::stripTrailing).toList();
            int from = 0;
            int to = lines.size();
            while (from < to && lines.get(from).isEmpty()) {
                from++;
            }
            while (to > from && lines.get(to - 1).isEmpty()) {
                to--;
            }
            return lines.subList(from, to);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
