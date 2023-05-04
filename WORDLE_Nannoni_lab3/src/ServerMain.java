
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.ServerSocket;


/**
 * @author fedenanno
 * Questo file è di proprietà di: fedenanno, ogni suo utilizzo va 
 * concordato con l'autore.
 * Creato in data: 
 * 
 */

public class ServerMain {
    
    private String word;
    
    ServerMain(){
        
    }

    //metodo che apre un file json 

    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(10000)) {
            
            System.out.println("The server is running on "+listener.getInetAddress()+" ...");
            
            ExecutorService pool = Executors.newFixedThreadPool(20);
            
            while (true) {
                pool.execute(new ServerTask(listener.accept()));
            }
        }
    }
    
}

