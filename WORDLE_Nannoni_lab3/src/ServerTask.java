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
    private volatile String goalWord;
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<String, String> words;

    private String[] hint;
    
    ServerTask(Socket socket, ConcurrentHashMap<String, String> words, ConcurrentHashMap<String, User> users) {
        this.socket = socket;

        this.users = users;
        this.words = words;

        this.hint = new String[10];
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
    public int checkWord(String word){
        if(word.length() != this.goalWord.length()){
            return -1;
        }
        if(word.equals(this.goalWord)){
            return 1;
        }
        //controlla che la parola sia presente nella hashmap words
        if(!words.contains(word)){
            return -1;
        }
        //controlla ogni lettera della parola
        int i = 0;
        for(char c : word.toCharArray()){
            if(c == this.goalWord.charAt(i)){
                hint[i] = "+";
            }
            else if(this.goalWord.contains(""+c)){
                hint[i] = "?";
            }
            else{
                hint[i] = "X";
            }
            i++;
        }
        return 0;
    }
    
    //TODO: capire come far riparire la partita quando cambia la parole

    /*
     * Controlla che l'username non sia presente nella hashmap users
     */
    public int register(String username, String password){
        if(users.containsKey(username)){
            return -1;
        }
        users.put(username, new User(username, password));
        return 0;
    }
    /*
        Richiede all'utente di inserire username e password, viene fatta una 
        ricerca nell'hash map e si controlla la password.
    */
    public int login(String username, String password){
        return 0;
    }
    
    public int loadUser(){
        return 0;
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
                cmd = in.nextLine();//.split(",");
                //System.out.println("comando: "+(String)cmd[0]);
                
                /*
                * eseguo il comando: 
                * 0 nuova partita, 
                * 1 combatti
                * 2 bevi pozione
                * 3 esci dal gioco
                */
                switch(cmd){
                    case "0" :
                        out.printf("0,\n");
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

