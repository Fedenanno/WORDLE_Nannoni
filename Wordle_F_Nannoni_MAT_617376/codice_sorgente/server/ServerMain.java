
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * @author fedenanno
 * Questo file è di proprietà di: fedenanno, ogni suo utilizzo va 
 * concordato con l'autore.
 * Creato in data: 
 * 
 */

public class ServerMain {

    //Server Config
    public int MAX_TRIES;
    public int TIME_TO_NEW_WORD; //secondi
    public int MAX_WORD_CHAR;
    
    //Multicast config
    //indirizzo e porta UDP
    public String[] UDP_INFO;
    //Lista di messaggi da mandare su socket udp
    private ConcurrentLinkedQueue<String> messaggiUDP;
    
    //variabili di stato
    //parola da indovinare
    private volatile String word;
    //Utenti registrati
    private ConcurrentHashMap<String, User> users;
    //Vocabolario di parole
    private ConcurrentHashMap<String, String> words;
    //statistiche di gioco della partita attuale
    private ConcurrentHashMap<String, gameStat> gameStats;
    
    ServerMain(String userPath, String wordPath, Integer max_tries, Integer time_to_new_word, Integer max_word_char){
        
        //prova a prendere i dati dai degli utenti e le parole dai file
        try{
            FileManager fm = new FileManager();
            this.users = fm.getUserList((userPath));
            this.words = fm.getWord((wordPath));
        }
        catch(Exception e){System.err.println("Errore caricamento file, controllare path cartella");return;}
        
        //setta le impostazioni del server
        this.MAX_TRIES = max_tries;
        this.TIME_TO_NEW_WORD = time_to_new_word;
        this.MAX_WORD_CHAR = max_word_char;
        
        //inizializza le statistiche della partita attuale
        this.gameStats = new ConcurrentHashMap<>();
        //imposta la prima parola
        this.word = this.getNewWord();
        
        System.err.println("La parole è: "+this.word);

        
        //Inzializzo info UDP
        this.messaggiUDP = new ConcurrentLinkedQueue();
        //crea un array di stringhe con le info per la connessione UDP
        this.UDP_INFO = new String[2];
        this.UDP_INFO[0] = "224.0.0.1";
        this.UDP_INFO[1] = "11001";
    
        
        
    }
    //GET e SET Impostazioni server

    public int getMAX_TRIES() {
        return MAX_TRIES;
    }
    
    public int getMAX_WORD_CHAR(){
        return this.MAX_WORD_CHAR;
    }
    
    public int getTIME_TO_NEW_WORD(){
        return this.TIME_TO_NEW_WORD;
    }
    
    
    //GET SET strutture dati
    public User getUsers(String username) {
        return this.users.get(username);
    }
    
    public void updateUser(User user){
        this.users.replace(user.getUsername(), user);
    }

    public void setUsers(User user) {
        //System.out.println("impostato nuovo utente");
        this.users.put(user.getUsername(), user);
        //System.out.println("Utenti: \n"+this.users);
        
    }
    
    public boolean containsUser(String username){
        return this.users.contains(username);
    }

    public String getWords(String word) {
        return words.get(word);
    }

    public gameStat getGameStats(String username) {
        return this.gameStats.get(username);
    }
    
    public ConcurrentHashMap<String, gameStat> getGameStatObject(){
        return this.gameStats;
    }

    public void setGameStats(gameStat gamestat) {
        this.gameStats.put(gamestat.getUsername(), gamestat);
    }

    public ConcurrentHashMap<String, User> getUsersObject() {
        return users;
    }
    
    public void addNewMessage(String message){
        this.messaggiUDP.add(message);
    }
    
    //Salva le statistiche. Metodo synch perche esegue operazioni multiple sulle strutture dati
    public synchronized void updateUserStat(){
        //salvo i dati delle partite attuali
        //guardo solo gli utenti che hanno giocato la partita attuale
        for(String username : this.gameStats.keySet()){
            //per ogni utente, aggiorno i suoi dati
            User user = this.getUsers(username);
            user.setGamePlayed(user.getGamePlayed()+1);
            
            //se ha vinto
            if(this.getGameStats(username).getWins()){
                //aggiorno il winRate e le streaks
                user.setWins(user.getWins()+1);
                user.setWinsRate();
                
                user.setLastStreak(user.getLastStreak()+1);
                
                //se la streak attuale è > alla bestSreak l'aggiorno
                if(user.getBestStreak() < user.getLastStreak())
                    user.setBestStreak(user.getLastStreak());
                
                //cambiare la distribuzione
                user.setDistribution(this.getGameStats(user.getUsername()).getTrys());
            }
            //l'utente ha perso
            else{
                //imposto il winRate
                user.setWinsRate();
                
                //resetto la streak attuale
                user.setLastStreak(0);
            }
            
        }
        
        System.err.println("\nStatistiche di gioco aggiornate!\n");
    }
    
    //GET e SET - WORD
    
    //prende una parola casuale dalla hashmap words
    private String getNewWord() {
        // Ottiene un array di chiavi dalla mappa
        String[] keys = this.words.keySet().toArray(new String[0]);

        // Genera un indice casuale basato sulla lunghezza dell'array di chiavi
        int randomIndex = ThreadLocalRandom.current().nextInt(keys.length);

        // Ottiene la chiave casuale dall'array di chiavi
        String randomKey = keys[randomIndex];

        // Restituisce il valore corrispondente alla chiave casuale
        return words.get(randomKey);
    }

    public String getWord() {
        return this.word;
    }

    public void setNewWord(){
        this.word = this.getNewWord();
    }
    
    //synch. in questo modo solo questo thread puo accedere alle risorse mentre le modifica
    public synchronized void changeWord(){
        
        //salvo le statistiche dei giocatori
        this.updateUserStat();
        
        //inizializzo i dati delle partite
        this.gameStats.clear();
        
        //cambio la parola di gioco
        this.setNewWord();
        
        System.err.println("\nParola cambiata!");
        System.out.println("parola: "+this.word+"\n");
    } 
    
    

    public static void main(String[] args) throws Exception {
        //DEBUG stampa la directory attuale
        //System.out.println(System.getProperty("user.dir"));
        
        Integer port = 0;
        Integer max_tries = 0;
        Integer max_word_char = 0;
        Integer time_to_new_word = 0;
        //carico i dati per il server usando la librearia gson
        //carica un file json con il seguente formato
        /*
         {
            "serverPort" : "1000",
            "max_tries" : "12",
            "max_word_char": "10",
            "time_to_new_word" : "60"
         }
         */
        //se non trova il file, manda un errore in console
        String filePath = "serverConfig.json";
        String userPath = "user.json";
        String wordPath = "words.txt";

        try {
            // Creare un parser JSON utilizzando la libreria Gsons
            // Leggere il contenuto del file JSON utilizzando il FileReader di Java
            // Estrarre le informazioni dal JSON e assegnarle alle variabili corrispondenti
            JsonObject jsonObject = new JsonParser().parse(new FileReader(filePath)).getAsJsonObject();
            //esegui il cast ad intero per le seguenti variabili
            port = jsonObject.get("serverPort").getAsInt();
            max_tries = jsonObject.get("max_tries").getAsInt();
            max_word_char = jsonObject.get("max_word_char").getAsInt();
            time_to_new_word = jsonObject.get("time_to_new_word").getAsInt();

            System.out.println("Server Port: " + port);
            System.out.println("Max Tries: " + max_tries);
            System.out.println("Max Word Char: " + max_word_char);
            System.out.println("Time to New Word: " + time_to_new_word);

        } catch (Exception e) {
            System.err.println("Errore caricamento file server");
        }
        
        
        //server TCP
        try (ServerSocket listener = new ServerSocket(port)) {
            
            System.out.println("The server is running on "+listener.getInetAddress()+" ...");
            
            ExecutorService pool = Executors.newFixedThreadPool(20);          
            
            //crea la classe con i dati
            ServerMain sm = new ServerMain(userPath, wordPath, max_tries, time_to_new_word, max_word_char);
            
            //salvataggio dati in caso di interruzzione del server
            Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
                public void run() {
                    try{
                        //sloggo gli utenti
                        //scorre solo gli utenti che sono attivi e li setta a false. Trova gli utenti attivi tramite una funzione lambda
                        for(User user : sm.getUsersObject().values().stream().filter(u -> u.isAttivo()).collect(Collectors.toList())){
                            user.setAttivo(false);
                        }
                        
                        FileManager fm = new FileManager();
                        fm.saveUser(sm.getUsersObject(), userPath);
                        
                        //chiudo il pool di thread
                        pool.shutdownNow();

                        System.out.println("\nDati salvati correttamente e utenti sloggati!\n");
                    }catch(Exception e){
                        System.err.println("Errore nella chiusura del server. Dati non salvati");
                    }
                }
            }); 
            
            //DEBUG stamap gli elementi della hastable users
            System.out.println("Utenti: \n"+sm.users);
            

            //fa partire un thread che si occupa di cambiare parola
            pool.execute(new Thread(){
                @Override
                public void run(){
                    while(true){
                        try {
                            System.out.println("\nThread cambio parola in esecuzione\n");
                            Thread.sleep(sm.getTIME_TO_NEW_WORD()*1000);
                            sm.changeWord();
                        } catch (InterruptedException ex) {
                            System.err.println("Errore cambio parola");
                        }}}});
            
            //faccio partire il thread che si occupa di leggere i messaggi degli utenti e inviarli all'inirizzo multicast
            pool.execute(new MulticastUDPManager(sm.messaggiUDP, sm.UDP_INFO ));

            //sm.addNewMessage("ciao come stai");

            //accetto nuove connessioni dai client
            while (true) {
                pool.execute(new ServerTask(listener.accept(), sm));
            }
        }
        catch(Exception e){
            System.err.println("Errore server, porta gia in uso. Cambiarla o terminare l'altro sofwtware!\n");
            //e.printStackTrace();
            return;
        }
    }
    
}

