
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author fedenanno
 * Questo file è di proprietà di: fedenanno, ogni suo utilizzo va 
 * concordato con l'autore.
 * Creato in data: 
 * 
 */

public class ServerMain {

    //Server Config
    public static final int MAX_TRIES = 12;
    public static final int TIME_TO_NEW_WORD = 100; //secondi
    public static final int MAX_WORD_CHAR = 10;
    
    //variabili di stato
    private volatile String word;
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<String, String> words;
    private ConcurrentHashMap<String, gameStat> gameStats;
    
    ServerMain(String pathToFileFolder){
        //prova a prendere i dati dai degli utenti e le parole dai file
        try{
            FileManager fm = new FileManager();
            this.users = fm.getUserList((pathToFileFolder+"/user.json"));
            this.words = fm.getWord((pathToFileFolder+"/words.txt"));
        }
        catch(Exception e){
            System.err.println("Errore caricamento file, controllare path cartella");
            return;
        }
        
        //inizializza le statistiche di gioco
        this.gameStats = new ConcurrentHashMap<>();

        //imposta la prima parola
        this.word = this.getNewWord();
        
        System.err.println("La parole è: "+this.word);


    }
    //GET e SET Impostazioni server

    public int getMAX_TRIES() {
        return MAX_TRIES;
    }
    
    public int getMAX_WORD_CHAR(){
        return ServerMain.MAX_WORD_CHAR;
    }
    
    public int getTIME_TO_NEW_WORD(){
        return ServerMain.TIME_TO_NEW_WORD;
    }
    
    
    //GET SET strutture dati
    public User getUsers(String username) {
        return this.users.get(username);
    }
    
    public void updateUser(User user){
        this.users.replace(user.getUsername(), user);
    }

    public void setUsers(User user) {
        System.out.println("impostato nuovo utente");
        this.users.put(user.getUsername(), user);
        System.out.println("Utenti: \n"+this.users);
        
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
    
    //Save stat
    
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
    
    //in questo modo solo questo thread puo accedere alle risorse mentre le modifica
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
        
        try (ServerSocket listener = new ServerSocket(10000)) {
            
            System.out.println("The server is running on "+listener.getInetAddress()+" ...");
            
            ExecutorService pool = Executors.newFixedThreadPool(20);
            
            //DEBUG stampa la directory attuale
            System.out.println(System.getProperty("user.dir"));
            
            //crea la classe con i dati
            ServerMain sm = new ServerMain("../file");
            
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
                        fm.saveUser(sm.getUsersObject(), "../file/user.json");
                        
                        System.out.println("\nDati salvati correttamente e utenti sloggati!\n");
                    }catch(Exception e){
                        System.err.println("Errore nella chiusura del server. Dati non salvati");
                    }
                }
            }); 
            
            //stamap gli elementi della hastable users
            System.out.println("Utenti: \n"+sm.users);
            
            // System.out.println("Parola: \n"+sm.getWord());
            // sm.setNewWord();
            // System.out.println("cambio parola: "+sm.getWord());

            //fa partire un thread che chiama il metodo sm.changeWord() e basta
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



            
            while (true) {
                pool.execute(new ServerTask(listener.accept(), sm));
            }
        }
        catch(Exception e){
            System.err.println("Errore server, porta gia in uso. Cambiarla o terminare l'altro sofwtware!");
        }
    }
    
}

