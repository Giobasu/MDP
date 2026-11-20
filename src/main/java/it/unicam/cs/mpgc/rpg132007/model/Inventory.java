package it.unicam.cs.mpgc.rpg132007.model;

/** Oro e pozioni dell'eroe. */
public class Inventory {

    private int gold;
    private int potions;

    public Inventory(int gold, int potions) {
        if (gold < 0 || potions < 0) {
            throw new IllegalArgumentException("Inventario non valido: " + gold + "/" + potions);
        }
        this.gold = gold;
        this.potions = potions;
    }

    public void addGold(int amount) {
        gold += amount;
    }

    /** Ritorna false (e non spende nulla) se l'oro non basta. */
    public boolean spendGold(int amount) {
        if (amount > gold) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public void addPotion() {
        potions++;
    }

    public boolean usePotion() {
        if (potions == 0) {
            return false;
        }
        potions--;
        return true;
    }

    public int getGold() {
        return gold;
    }

    public int getPotions() {
        return potions;
    }
}
