package it.unicam.cs.mpgc.rpg132007.battle;

/**
 * Una riga del log di gioco. Il tono dice alla view come evidenziarla, senza che il gioco conosca i colori.
 * heroHp ed enemyHp sono gli HP subito dopo l'evento (-1 se il messaggio non riguarda la battaglia):
 * la view li usa per animare le barre un messaggio alla volta, anche se il turno è già stato calcolato tutto.
 * fx (facoltativo) descrive l'animazione della mossa che il messaggio annuncia.
 */
public record Message(String text, Tone tone, int heroHp, int enemyHp, Fx fx) {

    public enum Tone { HERO, ENEMY, INFO, REWARD, DANGER }

    /**
     * Animazione di una mossa: dove compare (lato nemico o lato eroe) e se è un aiuto (cura, potenziamento)
     * invece di un colpo. fromHero dice chi la usa, per lo slancio.
     */
    public record Fx(boolean onEnemySide, boolean fromHero, boolean support) {
    }

    public static final int NO_HP = -1;

    public Message(String text, Tone tone) {
        this(text, tone, NO_HP, NO_HP, null);
    }

    public Message(String text, Tone tone, int heroHp, int enemyHp) {
        this(text, tone, heroHp, enemyHp, null);
    }

    public boolean hasHp() {
        return heroHp != NO_HP;
    }
}
