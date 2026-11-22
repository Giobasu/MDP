package it.unicam.cs.mpgc.rpg132007.core;

import java.util.EnumMap;
import java.util.Map;

/**
 * Base comune di eroe e nemici: statistiche, danni subiti, cure e stati alterati.
 * Modificatori e stati alterati durano solo per la battaglia in corso ({@link #restore()} li azzera).
 */
public abstract class Combatant {

    /** Con difesa pari a DEFENSE_SCALE il danno si dimezza. */
    public static final int DEFENSE_SCALE = 40;
    public static final int MAX_STAGE = 6;
    /** Ogni livello di modificatore vale 1/STAGE_DIVISOR della statistica (4 = 25%). */
    private static final int STAGE_DIVISOR = 4;

    private final String name;
    private final Appearance appearance;
    private final Map<Stat, Integer> stages = new EnumMap<>(Stat.class);
    private int level;
    private Stats stats;
    private int hp;
    private Status status;
    private int statusTurns;

    protected Combatant(String name, int level, Stats stats, Appearance appearance) {
        this.name = name;
        this.level = level;
        this.stats = stats;
        this.appearance = appearance;
        this.hp = stats.maxHp();
    }

    /**
     * Applica il danno, ridotto in proporzione alla difesa se ignoreDefense è false.
     * La riduzione è proporzionale e non sottrattiva: la difesa alta non azzera mai i colpi deboli
     * e ignorarla non diventa esagerato a livelli alti. Ritorna il danno effettivo (almeno 1).
     */
    public int takeDamage(int rawDamage, boolean ignoreDefense) {
        int defense = ignoreDefense ? 0 : getDefense();
        int damage = Math.max(1, rawDamage * DEFENSE_SCALE / (DEFENSE_SCALE + defense));
        hp = Math.max(0, hp - damage);
        return damage;
    }

    /** Cura fino al massimo. Ritorna gli HP effettivamente recuperati. */
    public int heal(int amount) {
        int before = hp;
        hp = Math.min(stats.maxHp(), hp + amount);
        return hp - before;
    }

    /** Sposta il modificatore di una statistica, tra -MAX_STAGE e +MAX_STAGE. Ritorna di quanto è cambiato davvero. */
    public int changeStage(Stat stat, int delta) {
        int before = getStage(stat);
        stages.put(stat, Math.clamp(before + delta, -MAX_STAGE, MAX_STAGE));
        return getStage(stat) - before;
    }

    public int getStage(Stat stat) {
        return stages.getOrDefault(stat, 0);
    }

    /** Ogni livello positivo vale +25% (+6 = x2.5); quelli negativi dividono allo stesso modo (-1 = x0.8, -6 = x0.4). */
    private int withStage(int value, Stat stat) {
        int stage = getStage(stat);
        return stage >= 0
                ? value * (STAGE_DIVISOR + stage) / STAGE_DIVISOR
                : value * STAGE_DIVISOR / (STAGE_DIVISOR - stage);
    }

    /**
     * Prova ad applicare uno stato alterato per qualche turno.
     * Fallisce (ritorna false) se ne ha già uno, se è a terra o se è immune.
     */
    public boolean inflict(Status newStatus, int turns) {
        if (status != null || !isAlive() || isImmuneTo(newStatus)) {
            return false;
        }
        status = newStatus;
        statusTurns = turns;
        return true;
    }

    /** Le sottoclassi possono rendere immuni ad alcuni stati (es. i boss al sonno). */
    protected boolean isImmuneTo(Status s) {
        return false;
    }

    public Status getStatus() {
        return status;
    }

    /** Consuma un turno dello stato; quando arriva a zero lo stato sparisce. Ritorna true se è appena guarito. */
    public boolean countDownStatus() {
        if (status == null) {
            return false;
        }
        statusTurns--;
        if (statusTurns <= 0) {
            status = null;
            statusTurns = 0;
            return true;
        }
        return false;
    }

    /** Toglie una % degli HP massimi (veleno, bruciatura), ignorando la difesa. Ritorna il danno. */
    public int loseHpPercent(int percent) {
        int damage = Math.max(1, stats.maxHp() * percent / 100);
        hp = Math.max(0, hp - damage);
        return damage;
    }

    /** Riporta il combattente in piena forma (inizio battaglia). */
    public void restore() {
        hp = stats.maxHp();
        status = null;
        statusTurns = 0;
        stages.clear();
    }

    public boolean isAlive() {
        return hp > 0;
    }

    protected void setLevel(int level) {
        this.level = level;
    }

    protected void setStats(Stats stats) {
        this.stats = stats;
        hp = Math.min(hp, stats.maxHp());
    }

    public String getName() {
        return name;
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public int getLevel() {
        return level;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return stats.maxHp();
    }

    /** Attacco effettivo: modificatori compresi, ridotto dagli stati che lo indeboliscono (es. bruciatura). */
    public int getAttack() {
        int attack = withStage(stats.attack(), Stat.ATTACK);
        return status == null ? attack : attack * status.attackPercent() / 100;
    }

    /** Difesa effettiva, modificatori compresi. */
    public int getDefense() {
        return withStage(stats.defense(), Stat.DEFENSE);
    }
}
