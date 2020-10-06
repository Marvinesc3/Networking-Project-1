package bbca;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;



public class ServerClientHandler implements Runnable {
    // Maintain data about the client serviced by this thread
    private ClientConnectionData client;
    private ArrayList<ClientConnectionData> clientList;

    public ServerClientHandler(ArrayList<ClientConnectionData> clientList, ClientConnectionData client) {
        this.clientList = clientList;
        this.client = client;
    }

    /**
     * Broadcasts a message to all clients connected to the server.
     */
    public void broadcast(String msg) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    c.getOut().println(msg);
                    // c.getOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
        
    }

    public void broadcast(String msg, ClientConnectionData client) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if(!c.equals(client))
                        c.getOut().println(msg);
                    // c.getOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
        
    }

    public void broadcast(String msg, String username) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if(c.getUserName().equals(username))
                        c.getOut().println(msg);
                    // c.getOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
        
    }

    public boolean isValid(String name) {
        for(ClientConnectionData c : clientList) {
            if(c.getUserName() != null && c.getUserName().equals(name))
                return false;
        }

        Pattern pattern = Pattern.compile("[^\\W]+");
        Matcher matcher = pattern.matcher(name);

        return matcher.matches();
    }

    @Override
    public void run() {
        try {
            BufferedReader in = client.getInput();
            //get userName, first message from user
            String userName = in.readLine().trim();
            while(!isValid(userName)) {
                client.getOut().println("Invalid username! Username taken or name contains a non-word character");
                userName = in.readLine().trim();
            }

            client.setUserName(userName.trim());
            //notify all that client has joined

            synchronized (clientList) {
                clientList.add(client);
            }
            
            broadcast(String.format("WELCOME %s", client.getUserName()));

            
            String incoming = "";
            while( (incoming = in.readLine()) != null) {
                String chat = incoming.trim();

                if (incoming.startsWith("QUIT")){
                    break;
                } else if (incoming.startsWith("@")){
                    try {
                        Pattern p = Pattern.compile("(@[^\\W]+) (.*)");
                        Matcher m = p.matcher(chat);
                
                        Boolean match = m.matches();
                        String recipient = m.group(1);

                        broadcast(client.getUserName() + " " + chat.replaceFirst(recipient, "[private]:"), recipient.substring(1));
                    } catch(Exception e){
                        System.out.println("Match not found");
                    }
                } else {
                    String[] strarr = chat.split(" ", 5);
                    for (int i = strarr.length - 1; i >= 0; i--){
                        if (strarr[i].startsWith(":") && strarr[i].endsWith(":")){
                            if (strarr[i].equals(":happy:")){
                                chat = "😃";
                            }
                            else if (strarr[i].equals(":sad:")){
                                chat = "😞";
                            }
                            else if (strarr[i].equals(":angry:")){
                                chat = "😠";
                            }
                            else if (strarr[i].equals(":crying:")){
                                chat = "😭";
                            } 
                            else if (strarr[i].equals(":lol:")){
                                chat = "😂";
                            }
                            else if (strarr[i].equals(":love:")){
                                chat = "🥰";
                            }
                            else if (strarr[i].equals(":cool:")){
                                chat = "😎";
                            }
                        }
                    }
                    String msg = String.format("%s:%s", client.getUserName(), chat);
                    broadcast(msg, client);
                }
            }
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket ex for " + 
                    client.getName());
            } else {
                System.out.println(ex);
                ex.printStackTrace();
            }
        } finally {
            //Remove client from clientList, notify all
            synchronized (clientList) {
                clientList.remove(client); 
            }
            System.out.println(client.getName() + " has left.");
            broadcast(String.format("EXIT %s", client.getUserName()));
            try {
                client.getSocket().close();
            } catch (IOException ex) {}

        }
    }
    
}
