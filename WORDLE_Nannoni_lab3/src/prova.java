// // import java.net.*;
// // import java.io.*;

// // public class Server {
// //     public static void main(String[] args) throws IOException {
// //         int tcpPort = 5000; // porta TCP
// //         int udpPort = 6000; // porta UDP
// //         MulticastSocket multicastSocket = new MulticastSocket(udpPort);
// //         InetAddress multicastGroup = InetAddress.getByName("224.0.0.1");
// //         multicastSocket.joinGroup(multicastGroup);
// //         // Creazione socket TCP
// //         ServerSocket serverSocket = new ServerSocket(tcpPort);
// //         while (true) {
// //             Socket clientSocket = serverSocket.accept();
// //             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
// //             String udpInfo = in.readLine(); // lettura porta UDP da client
// //             String[] udpData = udpInfo.split(":");
// //             String clientIP = clientSocket.getInetAddress().getHostAddress();
// //             int clientUDPPort = Integer.parseInt(udpData[1]);
// //             // Aggiungi il client alla lista dei client connessi
// //             ConnectedClient connectedClient = new ConnectedClient(clientIP, clientUDPPort);
// //             connectedClient.start(); // Avvia il thread per il client
// //         }
// //     }

// //     // Thread che si occupa di inviare i messaggi ai client connessi tramite UDP
// //     private static class ConnectedClient extends Thread {
// //         private String clientIP;
// //         private int clientUDPPort;

// //         public ConnectedClient(String clientIP, int clientUDPPort) {
// //             this.clientIP = clientIP;
// //             this.clientUDPPort = clientUDPPort;
// //         }

// //         public void run() {
// //             try {
// //                 // Crea un socket UDP per il client
// //                 DatagramSocket datagramSocket = new DatagramSocket();
// //                 InetAddress clientAddress = InetAddress.getByName(clientIP);
// //                 while (true) {
// //                     // Legge il messaggio dal client
// //                     BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
// //                     String message = in.readLine();
// //                     // Invia il messaggio al client tramite il socket multicast
// //                     byte[] buffer = message.getBytes();
// //                     DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastGroup, clientUDPPort);
// //                     datagramSocket.send(packet);
// //                 }
// //             } catch (IOException e) {
// //                 e.printStackTrace();
// //             }
// //         }
// //     }
// // }


// //----------------------CLIENT----------------------


// import java.net.*;
// import java.io.*;

// public class Client {
//     public static void main(String[] args) throws IOException {
//         String serverIP = "localhost";
//         int tcpPort = 5000; // porta TCP
//         int udpPort = 7000; // porta UDP
//         // Creazione socket TCP
//         Socket socket = new Socket(serverIP, tcpPort);
//         // Invio indirizzo IP e porta UDP al server
//         OutputStream out = socket.getOutputStream();
//         PrintWriter writer = new PrintWriter(out, true);
//         writer.println(serverIP + ":" + udpPort);
//         // Creazione socket UDP
//         DatagramSocket datagramSocket = new DatagramSocket(udpPort);
//         // Thread di ascolto dei messaggi dal server
//         Thread receiveThread = new Thread(() -> {
//             try {
//                 while (true) {
//                     byte[] buffer = new byte[1024];
//                     DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                     datagramSocket.receive(packet);
//                     String message = new String(packet.getData(), 0, packet.getLength());
//                     System.out.println(message);
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         });
//         receiveThread.start();
//         // Invio messaggi al server tramite socket TCP
//         while (true) {
//             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//             String message = in.readLine();
//             OutputStream outTCP = socket.getOutputStream();
//             PrintWriter writerTCP = new PrintWriter(outTCP, true);
//             writerTCP.println(message);
//         }
//     }
// }
