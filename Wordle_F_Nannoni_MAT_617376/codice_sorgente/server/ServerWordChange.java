/**
 *
 * @author federiconannoni
 */
public class ServerWordChange implements Runnable{
    
    //variabili di stato
    private ServerMain serverData;
    
    ServerWordChange(ServerMain sd){
        this.serverData = sd;
        
    }
    
    
    //cambia la parola.
    @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(this.serverData.getTIME_TO_NEW_WORD()*1000);
                    }
                    catch(Exception e){
                        System.err.println("Errore classe cambio parola!");
                    }

                    
                }
            }
}
