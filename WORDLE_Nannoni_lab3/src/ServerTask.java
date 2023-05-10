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

    private final Socket socket;
    
    //variabili di stato
    //in questo modo posso accedere alle strutture dati del server
    private final ServerMain serverData;

    //utente connesso nella sessione
    private User user;
    
    private boolean wordChange;
    
    //Emoji
    private String errorE;
    private String successE;
    private String winE;
    private String changeE;
    
    
    ServerTask(Socket socket, ServerMain serverData) {
        this.socket = socket;
        this.serverData = serverData;


        this.user = null;
        this.wordChange = false;
        

        //Creo le Emoji
        this.errorE = new String (Character.toChars(0x274C));
        this.successE = new String (Character.toChars(0x2705));
        this.winE = new String (Character.toChars(0x1F389));
        this.changeE = new String (Character.toChars(0x1F504));
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
            return this.errorE+"Login non effettuato";
        
        //System.out.println("|"+word+"|");
        
        //controlla che l'utente sia presente nella tabella gameStats
        gameStat gameStats = this.serverData.getGameStats(user.getUsername());
        if(gameStats != null){
            if(gameStats.getTrys() >= this.serverData.getMAX_TRIES())
                return this.errorE+"Numero di tentativi max superato!";
            if(gameStats.getWins() == true)
                return this.winE+"hai gia indovinato questa parola!";
            
            this.serverData.setGameStats(gameStats);
        }
        else{
            this.wordChange = true;
            return this.changeE+"La parola del giorno è cambiata! Inizia una nuova partita!";
        }
        
        //se la parola è piu lunga del max di char, errore
        if(word.length() > this.serverData.getMAX_WORD_CHAR())
            return this.errorE+"la parola deve essere di max. "+this.serverData.getMAX_WORD_CHAR()+" caratteri";
        
        //controlla che la parola sia presente nella hashmap words
        if(this.serverData.getWords(word) == null){
            return this.errorE+"La parola non è presente nel database!";
        }
        
        //conta il tentativo
        gameStats.setTrys(gameStats.getTrys()+1);
        
        //se la parola inviata è la stessa del server, ha vinto
        if(word.equals(this.serverData.getWord())){
            gameStats.setWins(true);
            return this.winE+this.winE+"Complimenti hai indovinato la parola di oggi!";
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

    /*
     * Controlla che l'username non sia presente nella hashmap users
     * synch. in modo che non venga creato nessun utente mentre ne sto gia creando uno.
     */
    public synchronized String register(String username, String password){
        if(this.user != null)
            return this.errorE+"Utente gia loggato";
        
        
        if(this.serverData.getUsers(username) != null){
            return this.errorE+"Username gia presente nel database!";
        }
        else{
            this.serverData.setUsers(new User(username, password));
            return this.successE+"Registrazione avvenuta, effettua il login con i seguenti dati: -username: "+username+" -password: "+password;
        }
    }
    /*
        Richiede all'utente di inserire username e password, viene fatta una 
        ricerca nell'hash map e si controlla la password.
    */
    
    //TODO qui c'e un nullPointerException
    public synchronized String login(String username, String password) throws Exception{
        if(this.user != null)
            return this.errorE+"Login gia effettuato";
        this.user = this.serverData.getUsers(username);
        if(this.user == null){
            this.user = null;
            return this.errorE+"Username non presente nel database, effettua la registrazione";
        }
        if(this.user.isAttivo()){
            this.user = null;
            return this.errorE+"questo utente è gia loggato nel sistema!";
        }
        if(this.user.getPassword() == null || !this.user.getPassword().equals(password)){
            this.user = null;
            return this.errorE+"Password errata";
        }        
        //adesso l'utente diventa attivo nel sistema
        user.setAttivo(true);
        
        return this.successE+"login effettuato con successo?"+this.serverData.UDP_INFO[0]+","+this.serverData.UDP_INFO[1];
        
    }
    //disconnette il player dalla sessione (ma non dalla partita, se gia iniziata)
    public String logout(){
        if(this.user == null)
            return this.errorE+"non hai effettuato il login!";
        //l'utente non è più attivo nel sistema
        this.user.setAttivo(false);
        
        this.user = null;
        return this.successE+"Logout effettuato, torna presto!";
    }
    
    //Si occupa di gestire l'inizio di una nuova partita
    public String playWORDLE(){
        if(this.user == null)
            return this.errorE+"Login non effettuato";
        
        //controlla se l'utente è presente nella tabella gameStats
        gameStat gameStats = this.serverData.getGameStats(user.getUsername());
        if(gameStats == null){
            this.serverData.setGameStats(new gameStat(user.getUsername()));
            this.wordChange = false;
            return this.successE+"Hai iniziato a partecipare a questa partita, manda la tua prima parola!";
        }
        return this.errorE+"Sei gia collegato a questa partita!";
        
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
            return this.errorE+"Login richiesto!";
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
        //DEBUG
        //System.out.println("Statistiche: \n"+ret);
        return ret;
    }
    
    //condivide la partita con il gruppo sociale (broadcast)
    public String share(){
        //DEBUG
        if(user == null){
            return this.errorE+"login richiesto";
        }
        //System.out.println("aggiungo il nuvovo messaggio alla coda");
        //aggiungo il messaggio alla coda UDP
        this.serverData.addNewMessage(this.user.getUsername()+"?"+this.sendMeStatistics());
        return this.successE+"condivido statistiche!";
    }
    
    //restituisce con chi hai condiviso la partita
    //se ne occupa il client di raccogliere i messaggi e mostrarli a video
    public String showMeSharing(){
        if(user == null)
            return this.errorE+"login richiesto";
        return "";
    }
    
    
    //metodo che gestisce la connessione, la ricezione e invio al client
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
                
                //DEBUG
                //System.err.println("Ricevuto comando cliente: "+cmd[0]);
                
                
                
                switch(cmd[0]){
                    case "login" :
                        if(this.user == null && cmd.length < 3){
                            //DEBUG
                            //System.err.println("Formato comando login erraro!");
                            out.println(this.errorE+"Errore formato comando: login <username> <password>");
                            break;
                        }
                        else if(this.user != null){
                            out.println(this.errorE+"Login gia effettuato!");
                            break;
                        }
                        
                        String ret = login(cmd[1], cmd[2]);
                        
                        out.println(ret);
                        break;
                     
                    case "logout":
                        
                        out.println(this.logout());
                        break;
                        
                    case "register":
                        
                        if(this.user == null && cmd.length < 3){
                            out.println(this.errorE+"Errore formato comando: register <username> <password>");
                            break;
                        }
                        if(this.user != null){
                            out.println(this.errorE+"Login gia effettuato!");
                            break;
                        }
                        out.println(this.register(cmd[1], cmd[2]));
                            
                        break;
                        
                    case "playWORDLE":
                        
                        out.println(this.playWORDLE());
                        
                        break;
                        
                    case "sendWord":
                        
                        if(cmd.length < 2){
                            out.println(this.errorE+"Fromato comando errato: sendWord <parola>");
                            break;
                        }
                        out.println(this.checkWord(cmd[1]));
                        
                        break;
                        
                    case "sendMeStatistics":
                        
                        out.println(this.sendMeStatistics());
                        
                        break;
                        
                    case "share":
                        
                        out.println(this.share());
                        
                        break;
                        
                    case "showMeSharing":
                        
                        out.println("");
                        
                        break;
                    default:
                        //errore, cmando non riconosciuto
                        //System.err.println(cmd[0].getClass());
                        out.printf(this.errorE+"404,\n");
                        break;
                    
                }
                
                cmd = null;
                
             }
            this.logout();
            System.out.println("Client Disconnesso;");
            
        } catch (Exception e) {
            System.err.println("Server Error: " + socket+"\n\n"+e);
            return;
        }
    }
}

