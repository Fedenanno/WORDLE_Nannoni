//NannonilabIII
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


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

    
    
    private User user;
    
    private boolean statCreated;
    private boolean wordChange;
    
    
    ServerTask(Socket socket, ServerMain serverData) {
        this.socket = socket;
        this.serverData = serverData;

        
        
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
        
        System.out.println("|"+word+"|");
        
        //controlla che l'utente sia presente nella tabella gameStats
        gameStat gameStats = this.serverData.getGameStats(user.getUsername());
        if(gameStats != null){
            if(gameStats.getTrys() >= this.serverData.getMAX_TRIES())
                return "Numero di tentativi max superato!";
            if(gameStats.getWins() == true)
                return "hai gia indovinato questa parola!";
            
            this.serverData.setGameStats(gameStats);
        }
        else{
            this.wordChange = true;
            return "La parola del giorno è cambiata! Inizia una nuova partita!";
        }
        
        //se la parola è piu lunga del max di char, errore
        if(word.length() > this.serverData.getMAX_WORD_CHAR())
            return "la parola deve essere di max. "+this.serverData.getMAX_WORD_CHAR()+" caratteri";
        
        //controlla che la parola sia presente nella hashmap words
        if(this.serverData.getWords(word) == null){
            return "La parola non è presente nel database!";
        }
        
        //conta il tentativo
        gameStats.setTrys(gameStats.getTrys()+1);
        
        //se la parola inviata è la stessa del server, ha vinto
        if(word.equals(this.serverData.getWord())){
            gameStats.setWins(true);
            return "Complimenti hai indovinato la parola di oggi!";
        }
        
        //se non ha indovinato, restituisce l'hint
        //controlla ogni lettera della parola
        int i = 0;
        
        String hint = "[";
        for(char c : word.toCharArray()){
            if(c == this.serverData.getWord().charAt(i)){
                hint += "+,";
            }
            else if(this.serverData.getWord().contains(""+c)){
                hint += "?,";
            }
            else{
                hint += "X,";
            }
            i++;
        }
        
        return hint+"]";
    }
    
    //TODO: capire come far riparire la partita quando cambia la parole

    /*
     * Controlla che l'username non sia presente nella hashmap users
     */
    public synchronized String register(String username, String password){
        if(this.user != null)
            return "Utente gia loggato";
        
        
        if(this.serverData.getUsers(username) != null){
            return "Username gia presente nel database!";
        }
        else{
            this.serverData.setUsers(new User(username, password));
            return "Registrazione avvenuta, effettua il login con i seguenti dati: -username: "+username+" -password: "+password;
        }
    }
    /*
        Richiede all'utente di inserire username e password, viene fatta una 
        ricerca nell'hash map e si controlla la password.
    */
    
    //TODO qui c'e un nullPointerException
    public synchronized String login(String username, String password){
        if(this.user != null)
            return "Login gia effettuato";
        this.user = this.serverData.getUsers(username);
        if(this.user == null)
            return "Username non presente nel database, effettua la registrazione";
        if(this.user.isAttivo()){
            this.user = null;
            return "questo utente è gia loggato nel sistema!";
        }
        if(this.user.getPassword() == null || !this.user.getPassword().equals(password))
            return "Password errata";
        
        //adesso l'utente diventa attivo nel sistema
        user.setAttivo(true);
        return "login effettuato con successo";
        
    }
    
    public String logout(){
        //l'utente non è più attivo nel sistema
        this.user.setAttivo(false);
        
        this.user = null;
        return "Logout effettuato, torna presto!";
    }
    
    //Si occupa di gestire l'inizio di una nuova partita
    public String playWORDLE(){
        if(this.user == null)
            return "Login non effettuato";
        
        //controlla se l'utente è presente nella tabella gameStats
        gameStat gameStats = this.serverData.getGameStats(user.getUsername());
        if(gameStats == null){
            this.serverData.setGameStats(new gameStat(user.getUsername()));
            this.wordChange = false;
            return "Hai iniziato a partecipare a questa partita, manda la tua prima parola!";
        }
        return "Sei gia collegato a questa partita!";
        
    }
    
    /*ritorna le statistiche del giocatore dopo l'ultima partita
    //restituendo un grafico ascii semplificato preso da user.get
    //formato in questo modo:
    //  +-----Statistic----+
    1: ** , 6 ^
    2:    , 2 ^
    ...
    */ 
    public String sendMeStatistics(){
        if(this.user == null){
            return "Login richiesto!";
        }
        String ret = "+---Statistic---+£Partite giocate: "+user.getGamePlayed()+"£win: "+user.getWins()+"£streak: "+user.getLastStreak()+"£best streak: "+user.getBestStreak()+"£";
        for(int i = 1; i <= this.serverData.getMAX_TRIES(); i++){
            Integer tryes = this.user.getDistribution(i);
            ret += i+": ";
            if(tryes > 1)
                for(int j = 0; j < tryes/3; j++)
                    ret +="*";
                
            ret += " , "+tryes+"£";
        }
        System.out.println("Statistiche: \n"+ret);
        return ret;
    }
    
    //condivide la partita con il gruppo sociale (broadcast)
    public String Share(){
        return "";
    }
    
    //restituisce con chi hai condiviso la partita
    public String showMeSharing(){
        return "";
    }
    
    
    @Override
    public void run() {
        
        String[] cmd;
        boolean exit = false;
        
        //connessione al socket
        System.out.println("Connected: " + socket);
        
        //input steam
        try (Scanner in = new Scanner(socket.getInputStream());
            //output stream
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true)){
            
             while (in.hasNextLine() && !exit) {
                //ricevo messaggio
                cmd = in.nextLine().split(" ");

                /*
                * eseguo il comando: 
                * 0 login - ok
                * 1 logout - ok
                * 2 register - ok
                * 3 playWORDLE - ok
                * 4 sendWord (checkWord) - ok
                * 5 sendMeStatistics
                * 6 share
                * 7 showMeSharing
                */
                
                //nel caso in cui la parola cambi mentre il giocatore sta sempre gicando, gli viene mandato un avviso.
                if(this.wordChange){
                    //mandare un messaggio agli utente per inforamarli
                    this.wordChange = false;
                    System.err.println("La parola è cambiata!");
                }
                
                System.err.println("Ricevuto comando cliente: "+cmd[0]);
                
                
                
                switch(cmd[0]){
                    case "login" :
                        if(this.user == null && cmd.length < 3){
                            System.err.println("Formato comando login erraro!");
                            out.println("Errore formato comando: login <username> <password>");
                            break;
                        }
                        else if(this.user != null){
                            out.println("Login gia effettuato!");
                            break;
                        }
                        
                        String ret = login(cmd[1], cmd[2]);
                        //System.err.println("Username: "+cmd[1]+" pass: "+cmd[2]);
                        System.out.println("Mando risposta al client: "+ret);
                        
                        out.println(ret);
                        break;
                     
                    case "logout":
                        
                        out.println(this.logout());
                        break;
                        
                    case "register":
                        
                        if(this.user == null && cmd.length < 3){
                            out.println("Errore formato comando: register <username> <password>");
                            break;
                        }
                        else if(this.user != null){
                            out.println("Login gia effettuato!");
                            break;
                        }
                        out.println(this.register(cmd[1], cmd[2]));
                            
                        break;
                        
                    case "playWORDLE":
                        
                        out.println(this.playWORDLE());
                        
                        break;
                        
                    case "sendWord":
                        
                        if(cmd.length < 2){
                            out.println("Fromato comando errato: sendWord <parola>");
                            break;
                        }
                        out.println(this.checkWord(cmd[1]));
                        
                        break;
                        
                    case "sendMeStatistics":
                        
                        System.out.println("Mando le statistiche!\n");
                        out.println(this.sendMeStatistics());
                        
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
            System.err.println("Server Error: " + socket+"\n\n"+e);
            return;
        }
    }
}

