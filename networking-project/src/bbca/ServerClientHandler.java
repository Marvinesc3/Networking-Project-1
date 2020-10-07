package bbca;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
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
            
            broadcast(String.format("WELCOME %s", emoji(client.getUserName())));

            
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

                        broadcast(client.getUserName() + " " + emoji(chat.replaceFirst(recipient, "[private]:")), recipient.substring(1));
                    } catch(Exception e){
                        System.out.println("Match not found");
                    }
                } else {
                    String msg = String.format("%s:%s", client.getUserName(), emoji(chat));
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
