package it.unicam.cs.mpgc.rpg132007.persistence;

import java.io.IOException;

/**
 * Scrive i salvataggi in un thread separato, così l'interfaccia non si blocca mentre si scrive su disco.
 * <p>
 * Più salvataggi possono partire quasi insieme (es. salvataggio automatico dopo un boss e subito dopo SALVA).
 * Senza protezione due thread scriverebbero lo stesso file temporaneo nello stesso momento (race condition):
 * per questo la scrittura è {@code synchronized}, un thread alla volta.
 * <p>
 * synchronized però non garantisce l'ordine: il thread partito per secondo potrebbe scrivere per primo,
 * e poi il primo sovrascriverebbe una partita più recente con una più vecchia. Per evitarlo ogni salvataggio
 * riceve un numero progressivo e quelli superati da uno più recente già scritto vengono saltati.
 */
public class BackgroundSaver {

    private final SaveRepository repository;
    /** Ultimo numero assegnato a un salvataggio. */
    private long issued;
    /** Numero del salvataggio più recente già scritto su disco. */
    private long written;

    public BackgroundSaver(SaveRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Il repository dei salvataggi non può essere null");
        }
        this.repository = repository;
    }

    /** Scrive subito, nel thread di chi chiama. */
    public void saveNow(SaveData data) throws IOException {
        write(nextNumber(), data);
    }

    /**
     * Avvia la scrittura in un nuovo thread e ritorna subito; a scrittura finita avvisa il listener.
     * data è immutabile, quindi il thread non vede mai la partita a metà di una modifica.
     * Il thread non è daemon: se si chiude il gioco, la JVM aspetta che il salvataggio finisca.
     * Ritorna il thread, per chi vuole aspettarlo con join().
     */
    public Thread saveInBackground(SaveData data, SaveListener listener) {
        long number = nextNumber(); // assegnato qui, nell'ordine in cui i salvataggi sono stati chiesti
        Thread worker = new Thread(() -> writeAndNotify(number, data, listener), "salvataggio");
        worker.start();
        return worker;
    }

    private void writeAndNotify(long number, SaveData data, SaveListener listener) {
        try {
            write(number, data);
        } catch (IOException e) {
            listener.failed(e);
            return;
        }
        listener.saved();
    }

    private synchronized long nextNumber() {
        issued++;
        return issued;
    }

    private synchronized void write(long number, SaveData data) throws IOException {
        if (number < written) {
            return; // sul disco c'è già una partita più recente di questa
        }
        repository.save(data);
        written = number;
    }
}
