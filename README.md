# WORDLE_Nannoni
 Progetto Finale per il corso Laboratorio di reti presso il corso di Laurea Triennale in Informatica all'università di Pisa. Studente F. Nannoni. "WORDLE un gioco di parole"


Funzionamneto:

Server:
    Richiede come parametri di avvio:
        -ip ?
        -porta ?
        -nome file di parole
        -nome file per gli utenti


    All'avvio il server carica la lista dei player:
        -per ogni player crea un oggetto (User.java) e lo inserisce in una lista tramite FileManager.java.getUserList()
        -seleziona una parola causale dalla lista di parole, tramite FileManager.java.getNewWord()
        -avvia il socker di ascolto per le nuove richieste di connessione.

    -getNewWord():
        restituisce una parola casuale, restituendola al chiamante.

    -checkWord(String parola):
        Controlla che la parola sia all'interno delle parole permesse, se si, controlla le lettere della parola per restitire un array con i vari indizzi.

ServerWorker:
    All'avvio richiede all'utente di eseguire il login o di registarsi. Una volta autenticato, cerca nella hash map i dati dell'utente.


    -login():
        Richiede all'utente di inserire username e password, viene fatta una ricerca nell'hash map e si controlla la password. se tutto ok, gli vengono mandati i vari comandi che può eseguire.
        Altrimenti viene richiesto di riprovare.

    -register():
        Richiede all'utente di inserire username e password, viene fatta una ricerca nell'hash map e si controlla se l'username è già presente. Se non è presente, viene creato un nuovo oggetto User e viene aggiunto alla hashmap.
        Altrimenti viene richiesto di riprovare.

FileManager.java
    -getUserList(string filename):
        Carica tutti gli utenti presenti al momento dell'avvio nel file json passato tramite argomento. 
        Creando e restituendo una ArrayList di oggetti di tipo User.

    -getWord(string filename):
        Apre il file <filename> legge le parole e le salva in un hashmap

    -saveNewUser(User user, String filename):
        Apre il file <filename> in scrittura e aggiunge un nuovo elemento con i dati dell'user.



UDP Multicast
Quando avvio il server, 
- creo un gruppo multicast e lo imposto come globale, Aggiunngendoci anche il Server (ok)
- crea un'arraylist concorrente in cui i client andranno a scrivere le loro statistiche da madare (ok)
- faccio partire un thread che legge da questa lista e manda i messaggi sul gruppo multicast (ok)

Quando un client si connette
- controllo che il login sia corretto.
- se ok restituisce indirizzo e porta UDP del gruppo mutlicast

Quando un client richiede una share su gruppo Multicast
- il serverWorker aggiunge una nuova stringa con username,<messaggio> alla struttura dati messaggiUDP (in modo che il thread del sermerMain possa leggerlo e inviarlo a tutti)

Quando chiude il server
- niente, il socker si chiude e tutti i client udp di disconnettono, la struttura dati viene cancellata e fine.


Quando il client parte
- esegue il login, se ok
- crea un socket udp
- crea un thread che ascolta solo da quel socket e salva tutto in una lista
- quando il client richiede i messaggi share, viene stampata questa lista. (per poi essere cancellata)