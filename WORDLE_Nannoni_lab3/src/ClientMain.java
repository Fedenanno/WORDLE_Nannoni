
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
            String[] rsp = null;
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
                
                
                //riecvo comando da input tastiera
                System.out.println("-->Comandi disponibili:\n 1: Attacca!,\n 2: usa pozione,\n 3: Esci");
                line = scanner.nextLine();
                //manda comando a server
                out.println(line);
                //System.err.println(line);
                
                

                //ricevo risposta dal server
                rsp = in.nextLine().split(",");
                System.err.println(rsp[0]);
                
                
                
                System.out.println("\n");
                
            }
        } catch(Exception e){System.err.println("Errore Cliente.");}
        finally {
            scanner.close();
            in.close();
        }
    }

}
