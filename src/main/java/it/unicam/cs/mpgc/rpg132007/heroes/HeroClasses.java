package it.unicam.cs.mpgc.rpg132007.heroes;

import it.unicam.cs.mpgc.rpg132007.action.DamageMove;
import it.unicam.cs.mpgc.rpg132007.action.Effect;
import it.unicam.cs.mpgc.rpg132007.action.HealMove;
import it.unicam.cs.mpgc.rpg132007.action.Skill;
import it.unicam.cs.mpgc.rpg132007.action.StatusMove;
import it.unicam.cs.mpgc.rpg132007.model.Appearance;
import it.unicam.cs.mpgc.rpg132007.model.ResourceSprites;
import it.unicam.cs.mpgc.rpg132007.model.Stats;

import java.util.List;

import static it.unicam.cs.mpgc.rpg132007.model.Stat.ATTACK;
import static it.unicam.cs.mpgc.rpg132007.model.Stat.DEFENSE;
import static it.unicam.cs.mpgc.rpg132007.model.Status.*;

/**
 * Le classi giocabili del gioco base, un metodo per classe. Per aggiungerne una basta scrivere un nuovo metodo,
 * aggiungerlo alla lista {@link #classes} e mettere il suo sprite .txt accanto a questa classe nelle risorse.
 */
public final class HeroClasses implements HeroClassCatalog {

    /** L'ordine è quello delle carte nella schermata di scelta. */
    private final List<HeroClass> classes = List.of(warrior(), archer(), lancer());

    @Override
    public List<HeroClass> all() {
        return classes;
    }

    private static HeroClass warrior() {
        return new HeroClass("warrior", "Guerriero",
                "Resistente. Incassa i colpi e picchia duro.",
                new Appearance(sprite("warrior.txt"), "#e8c170"),
                new Stats(64, 10, 6), new Stats(11, 4, 1),
                List.of(
                        damage("Fendente", 1, 15, 160, 1, false,
                                "Un potente colpo di spada.", Effect.NONE),
                        self("Rafforzatore", 2, 8, "STATO",
                                "Alza molto la tua difesa.", Effect.raise(DEFENSE, 2)),
                        damage("Colpo di Scudo", 3, 10, 90, 1, false,
                                "Un colpo di scudo che paralizza il nemico per 3 turni.", Effect.inflict(PARALYSIS, 3)),
                        heal("Ripresa", 5, 5, 35),
                        self("Grido di Guerra", 7, 8, "STATO",
                                "Alza molto il tuo attacco.", Effect.raise(ATTACK, 2)),
                        damage("Spada Divina", 9, 8, 140, 1, true,
                                "Un colpo di spada che ignora la difesa nemica e la abbassa.", Effect.lower(DEFENSE, 1)),
                        damage("Squarcio Infuocato", 12, 5, 260, 1, false,
                                "Un fendente devastante che brucia il nemico per 3 turni.", Effect.inflict(BURN, 3))));
    }

    private static HeroClass archer() {
        return new HeroClass("archer", "Arciere",
                "Fragile ma letale. Veleno, sonno e raffiche di frecce.",
                new Appearance(sprite("archer.txt"), "#9be37b"),
                new Stats(54, 11, 3), new Stats(12, 4, 1),
                List.of(
                        damage("Doppio Tiro", 1, 15, 75, 2, false,
                                "Due frecce scoccate in rapida successione.", Effect.NONE),
                        damage("Freccia Avvelenata", 2, 10, 70, 1, false,
                                "Una freccia che avvelena il nemico per 4 turni.", Effect.inflict(POISON, 4)),
                        foe("Dardo Soporifero", 3, 5, "STATO",
                                "Addormenta il nemico per 2 turni. Non funziona sui boss.", Effect.inflict(SLEEP, 2)),
                        damage("Freccia di Luce", 5, 8, 160, 1, true,
                                "Una freccia di luce che ignora la difesa nemica.", Effect.NONE),
                        heal("Ripresa", 7, 5, 30),
                        damage("Tiro Spezzacorazza", 9, 8, 90, 1, false,
                                "Un tiro che abbassa molto la difesa nemica.", Effect.lower(DEFENSE, 2)),
                        damage("Pioggia di Frecce", 12, 5, 60, 5, false,
                                "Cinque frecce piovono sul nemico.", Effect.NONE)));
    }

    private static HeroClass lancer() {
        return new HeroClass("lancer", "Lanciere",
                "Equilibrato. Colpi che ignorano la difesa nemica.",
                new Appearance(sprite("lancer.txt"), "#7fb2ff"),
                new Stats(52, 9, 4), new Stats(12, 3, 1),
                List.of(
                        damage("Affondo", 1, 15, 100, 1, true,
                                "Un colpo di punta che ignora la difesa nemica.", Effect.NONE),
                        damage("Squarcio", 2, 10, 80, 1, false,
                                "Una ferita profonda che avvelena il nemico per 4 turni.", Effect.inflict(POISON, 4)),
                        self("Concentrazione", 3, 8, "STATO",
                                "Alza il tuo attacco e la tua difesa.",
                                Effect.raise(ATTACK, 1).and(Effect.raise(DEFENSE, 1))),
                        damage("Affondo Drenante", 5, 8, 110, 1, false,
                                "Un affondo che ti fa recuperare metà dei danni inflitti.", Effect.drain(50)),
                        damage("Giavellotto Infuocato", 7, 8, 150, 1, false,
                                "Un giavellotto in fiamme che brucia il nemico per 3 turni.", Effect.inflict(BURN, 3)),
                        damage("Triplo Affondo", 9, 8, 60, 3, true,
                                "Tre affondi rapidi che ignorano la difesa nemica.", Effect.NONE),
                        damage("Lancia Celeste", 12, 5, 200, 1, true,
                                "Il colpo supremo: ignora la difesa nemica e ti fa recuperare il 30% dei danni inflitti.", Effect.drain(30))));
    }

    private static Skill damage(String name, int level, int pp, int power, int hits, boolean piercing,
                                String description, Effect effect) {
        return new Skill(new DamageMove(name, description, power, hits, piercing, effect), level, pp);
    }

    /** Cura di una % degli HP massimi (non usabile con gli HP già al massimo). */
    private static Skill heal(String name, int level, int pp, int percentOfMaxHp) {
        return new Skill(new HealMove(name, percentOfMaxHp), level, pp);
    }

    /** Mossa di stato su se stessi (potenziamenti). */
    private static Skill self(String name, int level, int pp, String category, String description, Effect effect) {
        return new Skill(new StatusMove(name, category, false, description, effect), level, pp);
    }

    /** Mossa di stato sull'avversario (stati alterati, indebolimenti). */
    private static Skill foe(String name, int level, int pp, String category, String description, Effect effect) {
        return new Skill(new StatusMove(name, category, true, description, effect), level, pp);
    }

    private static List<String> sprite(String file) {
        return ResourceSprites.load(HeroClasses.class, file);
    }
}
