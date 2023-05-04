//NannonilabIII
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
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
    
    
    ServerTask(Socket socket) {
        this.socket = socket;
    }
    
    //gli estremi min e max inclusi
    private int RandomNumber(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }
    
    private void updateWord(){
        return;
    }

    //funzione che si occupa di controllare le parole inviate al server e restituisce gli indizi
    public int checkWord(String word){
        return 0;
    }
    
    public int registerUser(String data){
        return 0;
    }
    
    public int login(String data){
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
                        //inizia la parita, inizializzo vita e pozione
                        this.inizioPartita();
                        //mando i dati al client
                        out.printf("0,%d,%d,%d\n", this.vita, this.pozione, this.mostro);
                        break;
                        
                    case "1":
                        //combatti, decrementa vita a player e mostro
                        this.combatti();
                        //manda vittoria (client: chiede di giocare ancora)
                        if(this.mostro <= 0)
                            out.printf("100,\n");
                        //manda sconfitta e chiude la connessione
                        else if(this.vita <= 0){
                            out.printf("99,\n");
                            exit = true;
                        }
                        else
                            out.printf("1,%d,%d\n", this.vita, this.mostro);
                        break;
                        
                    case "2":
                        //bevi pozione
                        this.beviPozione();
                        out.printf("2,%d,%d\n", this.vita, this.pozione);
                        break;
                        
                    case "3":
                        //esci dal gioco
                        out.printf("99,\n");
                        exit = true;
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

