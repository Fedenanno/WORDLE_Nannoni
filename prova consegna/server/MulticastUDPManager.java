
//Usato dal server per mandare i messaggi presenti dentro messageQueue a tutti i client

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author federiconannoni
 */
public class MulticastUDPManager  extends Thread {




    private final ConcurrentLinkedQueue<String> messageQueue;
    private final int UDP_PORT;
    private final InetAddress multicastAddress;
    private MulticastSocket socket;

    public MulticastUDPManager(ConcurrentLinkedQueue<String> messageQueue, String[] udp_info) throws Exception {
        this.messageQueue = messageQueue;
        this.UDP_PORT = Integer.parseInt(udp_info[1]);
        
        this.multicastAddress = InetAddress.getByName(udp_info[0]);
        this.socket = new MulticastSocket(this.UDP_PORT);
        this.socket.joinGroup(multicastAddress);
    }

    @Override
    public void run() {
        
        System.out.println("\nGruppo multicast creato, server aggiunto\nMulticast Manager in esecuzione!\n");
        
        try {

            while (!Thread.currentThread().isInterrupted()) {
                // Check if there are messages in the queue
                String message = messageQueue.poll();
                
                if (message == null) {
                    // Queue is empty, sleep for a bit
                    Thread.sleep(100);
                    continue;
                }
                else{
                    //DEBUG
                    System.out.println("Mando il messaggio: "+message);
                    // Convert the message to bytes and send it to the multicast group
                    byte[] data = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, multicastAddress, UDP_PORT);
                    socket.send(packet);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (socket != null) {
            socket.close();
        }
    }
}

