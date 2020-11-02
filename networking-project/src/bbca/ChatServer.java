package bbca;

import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ChatServer {
    public static final int PORT = 54321;

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(100);

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Chat Server has been started.");
            System.out.println("Local IP: "
                    + Inet4Address.getLocalHost().getHostAddress());
            System.out.println("Local Port: " + serverSocket.getLocalPort());
        
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    
                    String name = socket.getInetAddress().getHostName();
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    ClientConnectionData client = new ClientConnectionData(socket, in, out, name);
                    
                    synchronized (ServerClientHandler.clientList) {
                        ServerClientHandler.clientList.add(client);
                    }

                    //handle client business in another thread
                    pool.execute(new ServerClientHandler(client));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }

            }
        } 
    }
}
