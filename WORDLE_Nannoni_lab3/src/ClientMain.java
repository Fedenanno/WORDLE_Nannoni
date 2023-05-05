
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

public class ClientMain {


    public static void main(String[] args) throws Exception {
        // if (args.length != 1) {
        //     System.err.println("Pass the server IP as the sole command line argument");
        //     return;
        // }
        Scanner scanner = null;
        Scanner in = null;
            //args[0]
        try (Socket socket = new Socket("0.0.0.0", 10000)) {
            System.out.println("Enter lines of text then EXIT to quit");
            scanner = new Scanner(System.in);
            in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            boolean end = false;
            boolean inizio = true;
            String rsp = null;
            String line = null;
            
            /*
                * eseguo il comando: 
                * 0 nuova partita, 
                * 1 combatti
                * 2 bevi pozione
                * 3 esci dal gioco
             */
            
            System.out.println("Benvenuto! \n");
            
            while(!end){
                
                //inizio la partita
                if(inizio){
                    System.out.println("Effettua login o la registrazione");
                    System.out.println("login username password\nregister username password");
                    inizio = false;
                }
                else{
                    System.out.println("-->Comandi disponibili:\n"+
                            "playWORDLE (inizia una nuova partita)\n" +
                            "sendWord <parola>\n" +
                            "sendMeStatistics (ricevi le tue statistiche)\n" +
                            "6 share (condividi i risultati della partita attuale)\n" +
                            "7 showMeSharing (guarda con chi stai condividendo la i risultati)");
                }
                
                //riecvo comando da input tastiera
                line = scanner.nextLine();
                //manda comando a server
                out.println(line);
                //System.err.println(line);
                
                

                //ricevo risposta dal server
                rsp = in.nextLine();//.split(",");
                System.err.println(rsp);
                
                
                
                System.out.println("\n");
                
            }
        } catch(Exception e){
            System.err.println("Errore Client. :\n"+ e);
        }
        finally {
            scanner.close();
            in.close();
        }
    }

}
