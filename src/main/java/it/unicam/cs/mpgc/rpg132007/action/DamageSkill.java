package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Element;

/**
 * Skill offensiva configurabile: elemento, potenza in % dell'attacco, numero di colpi, se ignora la difesa
 * e un effetto secondario applicato dopo i colpi. Usata sia dagli eroi sia per le mosse speciali dei nemici.
 */
public record DamageSkill(String name, int unlockLevel, int maxPp, Element element, int powerPercent, int hits,
                          boolean piercing, String description, Effect effect) implements Skill {

    /** Neutra, senza descrizione né effetto: comoda per i test e per le mosse semplici. */
    public DamageSkill(String name, int unlockLevel, int maxPp, int powerPercent, int hits, boolean piercing) {
        this(name, unlockLevel, maxPp, Element.NEUTRO, powerPercent, hits, piercing, "", Effect.NONE);
    }

    @Override
    public String category() {
        return piercing ? "PERFORANTE" : "FISICO";
    }

    @Override
    public String execute(Combatant user, Combatant target) {
        int effectiveness = element.effectivenessAgainst(target.getElement());
        int perHit = user.getAttack() * powerPercent / 100 * effectiveness / 100;
        int total = 0;
        int landed = 0;
        while (landed < hits && target.isAlive()) {
            total += target.takeDamage(perHit, piercing);
            landed++;
        }
        StringBuilder text = new StringBuilder(user.getName() + " usa " + name + "!");
        if (effectiveness > Element.NORMAL) {
            text.append("\nÈ superefficace!");
        } else if (effectiveness < Element.NORMAL) {
            text.append("\nNon è molto efficace...");
        }
        String detail = landed > 1 ? " (" + landed + " colpi)" : "";
        text.append("\n").append(target.getName()).append(" subisce ").append(total).append(" danni").append(detail).append(".");
        String extra = effect.apply(user, target, total);
        if (!extra.isEmpty()) {
            text.append("\n").append(extra);
        }
        return text.toString();
    }
}
