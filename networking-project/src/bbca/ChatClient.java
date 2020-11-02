package bbca;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ChatClient {
    private static Socket socket;
    private static ObjectInputStream socketIn;
    private static ObjectOutputStream out;
    
    public static void main(String[] args) throws Exception {
        Scanner userInput = new Scanner(System.in);
        
        System.out.println("What's the server IP? ");
        String serverip = "localhost";//userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = 54321;//userInput.nextInt();
        //userInput.nextLine();

        socket = new Socket(serverip, port);
        
        out = new ObjectOutputStream(socket.getOutputStream());
        socketIn = new ObjectInputStream(socket.getInputStream());
        // start a thread to listen for server messages
        ClientServerHandler listener = new ClientServerHandler(socketIn);
        Thread t = new Thread(listener);
        t.start();

        String line = userInput.nextLine().trim();
        while(!line.toLowerCase().startsWith("/quit")) {
            if (listener.state == 1) {
                out.writeObject(new Message(Message.HEADER_NAME, line));
                line = userInput.nextLine().trim();
            } else if (listener.state == 2) {
                if (line.startsWith("@")) {
                    boolean readingMentions = true;
                    ArrayList<String> names = new ArrayList<String>();
                    String message = "";

                    String[] contents = line.split(" ");

                    for (int i = 0; i < contents.length; i++) {
                        if (readingMentions) {
                            if (contents[i].startsWith("@")) {
                                names.add(contents[i].substring(1));
                            } else {
                                readingMentions = false;
                                message += contents[i];
                            }
                        } else {
                            message += contents[i];
                        }
                    }

                    if (message.length() > 0) {
                        out.writeObject(new Message(Message.HEADER_PCHAT, message, names));
                        line = userInput.nextLine().trim();
                    }
                } else if (line.toLowerCase().startsWith("/whoishere")) {
                    System.out.println("List of chat members:");
                    System.out.println(ClientServerHandler.namesList);
                    line = userInput.nextLine().trim();
                } else {
                    out.writeObject(new Message(Message.HEADER_CHAT, line));
                    line = userInput.nextLine().trim();
                }
            }
        }
        out.writeObject(new Message(Message.HEADER_QUIT));
        out.close();
        userInput.close();
        socketIn.close();
        socket.close();
        
    }
}
