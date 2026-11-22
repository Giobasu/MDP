# Asciipon

Asciipon è un JRPG a turni con grafica in stile ASCII, realizzato in Java con JavaFX.
I personaggi sono presi da **Patapon** (i nemici sono gli Zigoton), mentre le meccaniche
sono ispirate ai **Pokémon**.

Si sceglie un eroe (Guerriero, Arciere o Lanciere) e si affrontano stage di nemici sempre più forti,
con un boss ogni 5 stage. Elementi principali:

- **Mosse alla Pokémon**: ogni classe ha 7 mosse che si sbloccano salendo di livello. Se ne conoscono al massimo 4:
  per impararne una nuova bisogna dimenticarne una, oppure rinunciare.
- **PP**: ogni mossa ha un numero limitato di usi, che si ricaricano solo dopo un boss, dopo una sconfitta o alla fonte.
  L'attacco base si può usare sempre.
- **Stati alterati** (veleno, bruciatura, paralisi, sonno) e **colpi critici**.
- **Sentieri**: dopo ogni vittoria si sceglie tra un nemico normale, un nemico élite (EXP e oro doppi)
  o una fonte che ricarica i PP.
- **Oro e pozioni**, **salvataggio** in JSON (automatico dopo ogni boss).

Comandi: frecce + Invio (o Z) per scegliere, Esc (o X) per tornare indietro; si può usare anche il mouse.

## Come eseguire il progetto

### Prerequisiti

- Java 25
- Gradle non va installato: è incluso il Gradle Wrapper

### Build ed esecuzione

```bash
git clone <url-del-repository>
cd <nome-cartella>
./gradlew build     # compila ed esegue i test
./gradlew run       # avvia il gioco
```

Il salvataggio viene scritto in `~/.rpg132007/save.json`.

## Uso di strumenti di AI

Nella realizzazione del progetto è stato usato **Claude (Claude Code)** per:

- generare una prima versione del codice e dei test, poi studiata, verificata e adattata personalmente;
- inserire i commenti nel codice;
- creare l'interfaccia di gioco con JavaFX.

Per convertire sprite e animazioni da immagini `.png` a file `.txt` è stato usato il tool online
[Image to ASCII](https://www.ascii-magic.com/tools/image-to-ascii).

Le scelte di game design, i contenuti (classi, mosse, nemici, bilanciamento) e la verifica finale del funzionamento
sono stati curati personalmente.
