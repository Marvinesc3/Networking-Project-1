package bbca;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientServerHandler implements Runnable {
    private static BufferedReader socketIn;
    public static int state = 0; 
    // 0 - default state
    // 1 - name being validated
    // 2 - active state

    public ClientServerHandler(BufferedReader socketIn) {
        this.socketIn = socketIn;
    }
 
    @Override
    public void run() {
        try {
            String incoming = "";

            while( (incoming = socketIn.readLine()) != null) {
                if (incoming.startsWith("SUBMITNAME")) {
                    state = 1;
                    System.out.println("Welcome to the server! Please enter a valid username: ");
                } else if (incoming.startsWith("RESUBMITNAME")) {
                    System.out.println("Invalid username! Username taken or name contains a non-word character.");
                } else if (incoming.startsWith("CONFIRMNAME")) {
                    state = 2;
                } else if (incoming.startsWith("WELCOME")) {
                    String name = incoming.substring(7).trim();
                    System.out.println(String.format("%s has joined the server.", name));
                } else if (incoming.startsWith("CHAT")) {
                    String[] msg = incoming.split(" ", 3);
                    System.out.println(String.format("%s: %s", msg[1].trim(), msg[2].trim()));
                } else if (incoming.startsWith("PCHAT")) {
                    Pattern p = Pattern.compile("PCHAT ([^\\W]+) (.*)");
                    Matcher m = p.matcher(incoming);

                    Boolean match = m.matches();
                    String sender = m.group(1);
                    String msg = m.group(2);

                    System.out.println(String.format("%s [private]: %s", sender, msg));
                } else if (incoming.startsWith("EXIT")) {
                    String name = incoming.substring(4).trim();
                    System.out.println(String.format("%s has left the chat.", name));
                } else {
                    System.out.println(incoming);
                }
                
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}