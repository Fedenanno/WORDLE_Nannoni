//NannonilabIII
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author fedenanno
 * Questo file è di proprietà di: fedenanno, ogni suo utilizzo va 
 * concordato con l'autore.
 * Creato in data: 
 * 
 */

public class ServerTask implements Runnable {

    private Socket socket;
    
    //variabili di stato
    private ServerMain serverData;

    private String[] hint;
    
    private User user;
    
    private boolean statCreated;
    private boolean wordChange;
    
    
    ServerTask(Socket socket, ServerMain serverData) {
        this.socket = socket;

        this.hint = new String[10];
        
        this.user = null;
        this.statCreated = false;
        this.wordChange = false;
    }
    

    /*
     * metodo checkWord che controlla che la parola ricevura sia corretta.
     * 1) controlla non aver superato il numero massimo di tentativi
     * 2) controlla che sia presente nella hashmap words
     * se non è presente ritorna un errore
     * se si passa al punto 3
     * 3) controlla ogni lettera della parola, se è presente nella parola da indovinare
     *      e crea un array di segni cosi composto:
     *      X : la lettera non è presente nella parola da indovinare
     *      ? : la lettera è presente nella parola da indovinare ma non nella posizione corretta
     *      + : la lettera è presente nella parola da indovinare e nella posizione corretta
     *    esempio: parola da indovinare: casa
     *            parola inserita: ciao
     *           risultato: [+,X,?,?]
     *      e ritorna 0
     * 4) se la parola è stata indovinata ritorna 1
     */
    //il blocco è synchronized perche la parola non può cambiare mentre si controlla.
    /*
     Codici Ritorno:
    
    0 Parola presente ma non indovinata
    1 vittoria
    
    Errore:
    
    -1 parola non presente
    -2 superato numero di tentativi massimo
    -3 ha gia vinto
    
    
    */
    public synchronized String checkWord(String word){
        if(this.user == null)
            return "Login non effettuato";
        
        //controlla che l'utente sia presente nella tabella gameStats
        gameStat gameStats = this.serverData.gameStats.get(user.getUsername());
        if(gameStats != null){
            if(gameStats.getTrys() > this.serverData.getMAX_TRIES())
                return "Numero di tentativi max superato!";
            if(gameStats.getWins() == true)
                return "hai gia indovinato questa parola!";
            gameStats.setTrys(gameStats.getTrys()+1);
            this.serverData.gameStats.put(user.getUsername(), gameStats);
        }
        else{
            this.wordChange = true;
            return "La parola del giorno è cambiata! Inizia una nuova partita!";
        }
        
        //se la parola inviata è la stessa del server, ha vinto
        if(word.equals(this.serverData.getWord())){
            gameStat gmw = new gameStat(user.getUsername(), gameStats.getTrys(), true);
            this.serverData.gameStats.put(word, gmw);
            return "Complimenti hai indovinato la parola di oggi!";
        }
        //controlla che la parola sia presente nella hashmap words
        if(!this.serverData.getWord().contains(word)){
            return "La parola non è presente nel database!";
        }
        //controlla ogni lettera della parola
        int i = 0;
        for(char c : word.toCharArray()){
            if(c == this.serverData.getWord().charAt(i)){
                hint[i] = "+";
            }
            else if(this.serverData.getWord().contains(""+c)){
                hint[i] = "?";
            }
            else{
                hint[i] = "X";
            }
            i++;
        }
        return "hint";
    }
    
    //TODO: capire come far riparire la partita quando cambia la parole

    /*
     * Controlla che l'username non sia presente nella hashmap users
     */
    public synchronized String register(String username, String password){
        if(this.serverData.users.containsKey(username)){
            return "Username gia presente nel database!";
        }
        this.serverData.users.put(username, new User(username, password));
        return "Registrazione avvenuta:\nEffettua il login con i seguenti dati:\n-username: "+username+"\n-password: "+password;
    }
    /*
        Richiede all'utente di inserire username e password, viene fatta una 
        ricerca nell'hash map e si controlla la password.
    */
    public String login(String username, String password){
        User user = this.serverData.users.get(username);
        if(user == null)
            return "Username non presente nel database";
        if(!user.getPassword().equals(password))
            return "Password errata";
        
        this.user = user;
        return "login effettuato con successo";
        
    }
    
    public String logout(){
        return "";
    }
    
    //Si occupa di gestire l'inizio di una nuova partita
    public String playWORDLE(){
        if(this.user == null)
            return "Login non effettuato";
        
        //controlla se l'utente è presente nella tabella gameStats
        gameStat gameStats = this.serverData.gameStats.get(user.getUsername());
        if(gameStats == null){
            this.serverData.gameStats.put(user.getUsername(), new gameStat(user.getUsername(), 1));
            this.wordChange = false;
            return "Hai iniziato a partecipare a questa partita, manda la tua prima parola!";
        }
        return "Sei gia collegato a questa partita!";
        
    }
    
    //ritorna le statistiche del giocatore dopo l'ultima partita
    public String sendMeStatistics(){
        return "";
    }
    
    //condivide la partita con il gruppo sociale (broadcast)
    public String Share(){
        return "";
    }
    
    //restituisce con chi hai condiviso la partita
    public String showMeSharing(){
        return "";
    }
    
    
    

    public void run() {
        
        String cmd;
        boolean exit = false;
        
        //connessione al socket
        System.out.println("Connected: " + socket);
        
        //input steam
        try (Scanner in = new Scanner(socket.getInputStream());
            //output stream
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true)){
            
             while (in.hasNextLine() && !exit) {
                //ricevo messaggio
                cmd = in.nextLine().split(",")[0];
                //System.out.println("comando: "+(String)cmd[0]);
                
                /*
                * eseguo il comando: 
                * 0 login 
                * 1 logout
                * 2 register
                * 3 playWORDLE
                * 4 sendWord (checkWord)
                * 5 sendMeStatistics
                * 6 share
                * 7 showMeSharing
                */
                
                if(this.wordChange){
                    //mandare un messaggio agli utente per inforamarli
                    this.wordChange = false;
                }
                
                switch(cmd){
                    case "0" :
                        System.out.println("Comando cliente: "+cmd);
                        out.printf("0,come va\n");
                        break;
                        
                    default:
                        //errore, cmando non riconosciuto
                        //System.err.println(cmd[0].getClass());
                        out.printf("404,\n");
                        break;
                    
                }
                
                cmd = null;
                
             }
            
            System.out.println("Client Disconnesso;");
            
        } catch (Exception e) {
            System.err.println("Server Error: " + socket);
        }
    }
}

