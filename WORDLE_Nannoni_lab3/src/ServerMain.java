
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;


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
    public static final int TIME_TO_NEW_WORD = 6000; //secondi
    
    //variabili di stato
    private volatile String word;
    public ConcurrentHashMap<String, User> users;
    public ConcurrentHashMap<String, String> words;
    public ConcurrentHashMap<String, gameStat> gameStats;
    
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

        //fa partire un thread che ogni TIME_TO_NEW_WORD secondi cambia la parola
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(TIME_TO_NEW_WORD*1000);
                    }
                    catch(Exception e){
                        System.err.println("Errore sleep thread");
                    }
                    word = getNewWord();
                }
            }
        });
        t.start();

    }
    //GET e SET

    public int getMAX_TRIES() {
        return MAX_TRIES;
    }
    
    
    
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
    

    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(10000)) {
            
            System.out.println("The server is running on "+listener.getInetAddress()+" ...");
            
            ExecutorService pool = Executors.newFixedThreadPool(20);
            
            //DEBUG stampa la directory attuale
            System.out.println(System.getProperty("user.dir"));
            
            //crea la classe con i dati
            ServerMain sm = new ServerMain("../file");
            
            //stamap gli elementi della hastable users
            System.out.println("Utenti: \n"+sm.users);
            
            // System.out.println("Parola: \n"+sm.getWord());
            // sm.setNewWord();
            // System.out.println("cambio parola: "+sm.getWord());
            
            while (true) {
                pool.execute(new ServerTask(listener.accept(), sm));
            }
        }
    }
    
}

