
//Usato dai client per accettare connessioni in entrata (gruppo unicast)


import java.net.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author federiconannoni
 */
public class MulticastReceiverWorker  extends Thread {


    private MulticastSocket socket;
    private InetAddress address;
    private int port;
    private LinkedList<String> messages;
    private String username;

    public MulticastReceiverWorker(String multicastAddress, int port, String username) {
        try {
            this.socket = new MulticastSocket(port);
            this.address = InetAddress.getByName(multicastAddress);
            this.port = port;
            this.messages = new LinkedList<String>();
            this.socket.joinGroup(this.address);
            this.username = username;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        
        //DEBUG
        //System.out.println("Servizio di raccolta messaggi attivo");
        
        byte[] buffer = new byte[1024];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if(!message.split("\\?")[0].equals(this.username))
                    messages.add(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public LinkedList<String> getMessages() {
        return messages;
    }

    public static void main(String[] args) {
        MulticastReceiverWorker receiver = new MulticastReceiverWorker("224.0.0.1", 5000, "");
        receiver.start();

        // Wait for messages to be received
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> messages = receiver.getMessages();
        System.out.println("Received " + messages.size() + " messages:");
        for (String message : messages) {
            System.out.println(message);
        }
    }
}

