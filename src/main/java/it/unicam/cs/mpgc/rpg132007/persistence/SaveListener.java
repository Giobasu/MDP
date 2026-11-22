package it.unicam.cs.mpgc.rpg132007.persistence;

import java.io.IOException;

/**
 * Chi vuole sapere com'è andato un salvataggio fatto in background.
 * Attenzione: i metodi vengono chiamati dal thread di salvataggio, non da quello dell'interfaccia grafica.
 */
public interface SaveListener {

    /** Per chi non è interessato all'esito. */
    SaveListener IGNORE = new SaveListener() {
        @Override
        public void saved() {
        }

        @Override
        public void failed(IOException error) {
        }
    };

    void saved();

    void failed(IOException error);
}
