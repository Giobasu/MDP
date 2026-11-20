# ASCII Quest

JRPG a turni con grafica in stile ASCII (JavaFX). Scegli un eroe (Guerriero, Arciere o Lanciere) e affronta stage dopo
stage nemici sempre più forti, con un boss ogni 5 stage. Ogni classe ha 7 mosse che si sbloccano salendo di livello:
attacchi, colpi multipli, mosse che ignorano la difesa, veleno, risucchio di HP, potenziamenti e indebolimenti.
Le vittorie danno EXP e oro, l'oro compra pozioni. La partita si può salvare e ricaricare.

Ogni mossa ha dei PP (usi disponibili) che non si ricaricano tra una battaglia e l'altra: tornano al massimo
solo dopo aver sconfitto un boss o dopo una sconfitta. L'attacco base non ha PP e si può usare sempre.

Mosse e nemici hanno un elemento (FUOCO > NATURA > ACQUA > FUOCO, LUCE e OMBRA si battono a vicenda):
i colpi superefficaci fanno x1.5, quelli poco efficaci x0.6. Alcune mosse causano stati alterati
(veleno, bruciatura, paralisi, sonno; i boss non si addormentano).

Dopo ogni vittoria il sentiero si divide: sentiero normale, sentiero élite (nemico più forte, EXP e oro doppi)
o fonte (ricarica i PP ma non dà ricompense). Prima dei boss la strada è una sola.
Dopo ogni boss la partita si salva da sola; il salvataggio conserva anche i PP.

La battaglia si gioca come nei JRPG classici: menu LOTTA / ZAINO / STATO / FUGA, con frecce + Invio (o Z),
Esc (o X) per tornare indietro, oppure col mouse.

---

## Come eseguire il progetto

### Prerequisiti
- Java 25 (LTS)
- Gradle (non serve installarlo: è incluso il Gradle Wrapper)

### Istruzioni

```bash
git clone <url-del-repository>
cd <nome-cartella>
```

### Build del progetto
```bash
./gradlew build
```

### Esecuzione
```bash
./gradlew run
```

Il salvataggio viene scritto in `~/.rpg132007/save.properties`.

---

## Uso di strumenti di AI

Utilizzato Claude (Claude Code) per:
- progettare l'architettura (package, interfacce, responsabilità delle classi)
- generare una prima versione del codice e dei test, poi studiata, verificata e adattata personalmente

Dettagli nella Wiki del repository.
