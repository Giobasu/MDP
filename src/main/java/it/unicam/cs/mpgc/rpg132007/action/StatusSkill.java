package it.unicam.cs.mpgc.rpg132007.action;

import it.unicam.cs.mpgc.rpg132007.model.Combatant;
import it.unicam.cs.mpgc.rpg132007.model.Element;

/**
 * Skill senza danno diretto: cure, potenziamenti, indebolimenti, stati alterati. Cosa fa lo decide l'{@link Effect};
 * targetsEnemy dice solo se l'animazione va sull'avversario o su chi la usa.
 */
public record StatusSkill(String name, int unlockLevel, int maxPp, Element element, String category,
                          boolean targetsEnemy, String description, Effect effect) implements Skill {

    @Override
    public String execute(Combatant user, Combatant target) {
        String extra = effect.apply(user, target, 0);
        String text = user.getName() + " usa " + name + "!";
        return extra.isEmpty() ? text : text + "\n" + extra;
    }
}
