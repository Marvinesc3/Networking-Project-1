package bbca;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerClientHandler implements Runnable {
    // Maintain data about the client serviced by this thread
    ClientConnectionData client;
    public static final ArrayList<ClientConnectionData> clientList = new ArrayList<>();

    public ServerClientHandler(ClientConnectionData client) {
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

    public void broadcast(String msg, String username) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if(!(c.getUserName() == null) && client.getUserName().equals(username))
                        c.getOut().println(msg);
                    // c.getOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
        
    }

    public void broadcastExcept(String msg, ClientConnectionData client) {
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

    public void broadcast(String msg, ClientConnectionData client) {
        try {
            System.out.println("Broadcasting -- " + msg);            
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if(c.equals(client))
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

<<<<<<< HEAD

=======
>>>>>>> 60be2344e2accc4c7512abf19947244e4517d97e
    ArrayList<String> emojis = new ArrayList<>(Arrays.asList (":happy:", "😃", ":sad:", "😞", 
            ":angry:", "😠", ":crying:", "😭", ":lol:", "😂", ":love:", "😍", ":fire:", "🔥", 
            ":wink:", "😉", "kiss", "😘", "crazy", "🤪	", "money", "🤑", "shush", "🤫", "think", "🤔", 
            "meh", "😐", "gross", "🤢", "hot", "🥵", "party", "🥳", "poggers", "😲", "100", "💯", 
            "hands", "🙏", "strong", "💪", "eyes", "👀", "cap", "🧢"));
            
    public String emoji (String msg){
        String[] strarr = msg.split(" ");
        for (int i = strarr.length - 1; i >= 0; i--){
            if (strarr[0].equals("/list")){
                String emojilist = "";
                for(String s : emojis) 
                    emojilist += s + " / ";
                broadcast(emojilist);;
            }
            else if (strarr[i].startsWith(":") && strarr[i].endsWith(":")){

                for (int j = emojis.size()-2; j >= 0; j = j-2){
                    if (strarr[i].equals(emojis.get(j)))
                        msg = msg.replace(strarr[i], emojis.get(j+1));
                }
            }
        }
        return msg;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = client.getInput();
            String incoming = "";

            broadcast("SUBMITNAME", client);
            boolean validName = false;
            String userName = "";

            while (!validName && (incoming = in.readLine()) != null) {
                if (incoming.startsWith("NAME")) {
                    String name = incoming.substring(4).trim();
                    if (isValid(name)) {
                        validName = true;
                        userName = name;
                    }
                }

                if (!validName) {
                    broadcast("RESUBMITNAME", client);
                }
            }

            client.setUserName(userName.trim());
            broadcast("CONFIRMNAME", client.getName());
            //notify all that client has joined
<<<<<<< HEAD

            synchronized (clientList) {
                clientList.add(client);
            }
            
            broadcast(String.format("WELCOME %s", emoji(client.getUserName())));

            
            String incoming = "";
            
            while( (incoming = in.readLine()) != null) {
                String chat = incoming.trim();
                 

=======
            broadcast(String.format("WELCOME %s", client.getUserName()));

            incoming = "";
            while( (incoming = in.readLine()) != null) {
>>>>>>> 60be2344e2accc4c7512abf19947244e4517d97e
                if (incoming.startsWith("QUIT")){
                    break;
                } else if (incoming.startsWith("PCHAT")) {
                    String chat = incoming.trim();
                    try {
                        Pattern p = Pattern.compile("PCHAT ([^\\W]+) (.*)");
                        Matcher m = p.matcher(chat);
                 
                        Boolean match = m.matches();
                        String recipient = m.group(1);
<<<<<<< HEAD

                        broadcast(client.getUserName() + " " + emoji(chat.replaceFirst(recipient, "[private]:")), recipient.substring(1));
=======
                        String line = m.group(2);
                        
                        String msg = String.format("PCHAT %s %s", client.getUserName(), line);
                        broadcast(msg, recipient);
                        // broadcast(client.getUserName() + " " + emoji(chat.replaceFirst(recipient, "[private]:")), recipient.substring(1));
>>>>>>> 60be2344e2accc4c7512abf19947244e4517d97e
                    } catch(Exception e){
                        System.out.println("Match not found.");
                    }
                } else {
<<<<<<< HEAD
                    String msg = String.format("%s:%s", client.getUserName(), emoji(chat));
                    broadcast(msg, client);
=======
                    //CHAT
                    String chat = incoming.substring(4).trim();
                    String msg = String.format("CHAT %s %s", client.getUserName(), emoji(chat));
                    broadcastExcept(msg, client);
>>>>>>> 60be2344e2accc4c7512abf19947244e4517d97e
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