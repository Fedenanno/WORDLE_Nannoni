
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


/**
 * @author fedenanno
 * Questo file è di proprietà di: fedenanno, ogni suo utilizzo va 
 * concordato con l'autore.
 * Creato in data: 
 * 
 */

public class ClientMain {
    
    public static String formatString(String input) {
        return input.replaceAll("£", "\n");
    }
    
    public static String convertiEmoji(Integer code){
        return String.valueOf(Character.toChars(code));
        //new String (Character.toChars())
    }

    public static void main(String[] args) throws Exception {
        
        //carico le impostazioni dal file
                //se non trova il file, manda un errore in console
        String filePath = "clientConfig.json";
        
        Integer port = 0;
        String ip = "";

        try {
            // Creare un parser JSON utilizzando la libreria Gsons
            // Leggere il contenuto del file JSON utilizzando il FileReader di Java
            // Estrarre le informazioni dal JSON e assegnarle alle variabili corrispondenti
            JsonObject jsonObject = new JsonParser().parse(new FileReader(filePath)).getAsJsonObject();
            //esegui il cast ad intero per le seguenti variabili
            port = jsonObject.get("clientPort").getAsInt();
            ip = jsonObject.get("clientIp").getAsString();

            System.out.printf("Connesso al server: %s:%d\n\n", ip, port);

        } catch (Exception e) {
            System.err.println("Errore caricamento file server");
        }
        
        
        
        
        Scanner scanner = null;
        Scanner in = null;
            //args[0]
        try (Socket socket = new Socket(ip, port)) {
            System.out.println("Enter lines of text then EXIT to quit");
            scanner = new Scanner(System.in);
            in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            boolean end = false;
            boolean login = false;
            String rsp = null;
            String line = null;
            MulticastReceiverWorker mw = null;
            String user = null;
            
            
            /*
                * eseguo il comando: 
                * 0 nuova partita, 
                * 1 combatti
                * 2 bevi pozione
                * 3 esci dal gioco
             */
            
            /*
            
            lucchetto con chiave 0x1F510
            lucchetto con penna 0x1F50F
            joystick 0x1F579
            vittoria 1F3AF
            nuova partita 1F195
            condividi 1F4F2
            
            */
            
            System.out.println("Benvenuto! "+new String (Character.toChars(0x1F601))+"\n\n");
            
            while(!end){
                
                //inizio la partita
                if(!login){
                    System.out.println("Effettua login o la registrazione!");
                    System.out.println("->"+new String (Character.toChars(0x1F510))+"login <username> <password>\n->"+new String (Character.toChars(0x1F50F))+"register <username> <password>\n\n");
                }
                else{
                    System.out.println("\n----------------------------------------------------"+
                            "\n-->Comandi disponibili:\n"+
                            "->"+new String (Character.toChars(0x1F579))+" playWORDLE (inizia una nuova partita)\n" +
                            "->"+new String (Character.toChars(0x270F))+" sendWord <parola> (manda una parola)\n" +
                            "->"+new String (Character.toChars(0x1F4B1))+" sendMeStatistics (ricevi le tue statistiche)\n" +
                            "->"+new String (Character.toChars(0x1F4F2))+" share (condividi i risultati delle tue partite)\n" +
                            "->"+new String (Character.toChars(0x1F4E5))+" showMeSharing (guarda le statistiche condivise dagli altri utenti)\n\n");
                }
                
                //riecvo comando da input tastiera
                line = scanner.nextLine();
                
                if(line.equals("exit")){
                    if(mw != null)
                        mw.stop();
                    System.exit(0);
                }
                
                //controllo il comando
                if(line.split(" ")[0].equals("logout")){
                    login = false;
                    if(mw != null){
                        mw.interrupt();
                    }
                }
                    
                if(line.equals("showMeSharing")){
                    if(mw != null){
                        LinkedList<String> list = mw.getMessages();
                        System.out.println("Messaggi ricevuti:\n\n");
                        if(list.isEmpty())
                            System.out.println("Nessun messaggio da leggere");
                        else
                            while(!list.isEmpty()){
                                String s = list.poll();
                                try{
                                    System.out.println("da: "+s.split("\\?")[0]+"\nmessaggio: "+s.split("\\?")[1].replaceAll("£", "\n")+"\n------\n");
                                }catch(Exception e){
                                    System.out.println(s);
                                }}}}
                
                
                //manda comando a server
                out.println(line);
                //System.err.println(line);
                
                

                //ricevo risposta dal server
                rsp = in.nextLine();//.split(",");
                
                //nel caso ricevessi dati di login
                if(!login){
                    String[] tmp = rsp.split("\\?");
                    if(tmp.length > 1){
                        login = true;
                        //mi salvo l'username
                        user = line.split(" ")[1];
                        //fa partire il thread che legge i messaggi dal socket
                        mw = new MulticastReceiverWorker(tmp[1].split(",")[0], Integer.parseInt(tmp[1].split(",")[1]), user);
                        mw.start();
                        rsp = tmp[0];
                    }
                }

                System.out.println("\n");
                //nel caso ricevessi le stat. creo la stringa formattata correttamente
                System.err.println(rsp.replaceAll("£", "\n"));
                System.out.println("\n");
                
            }
        }catch(ConnectException ce){
            System.err.println("Errore Client. Nessun server trovato");
        }
        catch(Exception e){
            System.err.println("Errore Client. :\n"+ e);
            e.printStackTrace();
        }
        
        finally {
            scanner.close();
            in.close();
            
        }
    }

}
