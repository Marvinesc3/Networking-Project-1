package bbca;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.ObjectInputStream;



public class ServerClientHandler implements Runnable {
    // Maintain data about the client serviced by this thread
    ClientConnectionData client;
    public static final ArrayList<ClientConnectionData> clientList = new ArrayList<>();

    public ServerClientHandler(ClientConnectionData client) {
        this.client = client;
    }

    public void Broadcast(Message msg) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    c.getOut().writeObject(msg);
                    // c.getOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("Broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void BroadcastToClientName(Message msg, String name) {
        try {
            System.out.println(name + "Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getName() == name) c.getOut().writeObject(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("Broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void BroadcastToClientUserName(Message msg, String name) {
        try {
            System.out.println("Broadcasting -- " + msg);
            System.out.println(msg.getMsg());
            System.out.println(name);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getUserName().equals(name)) c.getOut().writeObject(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void BroadcastSkipUserName(Message msg, String name) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (!c.getUserName().equals(name)) c.getOut().writeObject(msg);
                }
            }
        } catch (Exception ex) {
            System.out.println("Broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public String emoji (String msg) {
        ArrayList<String> emojis = new ArrayList<>(Arrays.asList (":happy:", "😃", ":sad:", "😞", 
            ":angry:", "😠", ":crying:", "😭", ":lol:", "😂", ":love:", "😍", ":fire:", "🔥", 
            ":wink:", "😉", ":kiss:", "😘", ":crazy:", "🤪	", ":money:", "🤑", ":shush:", "🤫", ":think:", "🤔", 
            ":meh:", "😐", ":gross:", "🤢", ":hot:", "🥵", ":party:", "🥳", ":poggers:", "😲", ":100:", "💯", 
            ":hands:", "🙏", ":strong:", "💪", ":eyes:", "👀", ":cap:", "🧢"));

        String[] strarr = msg.split(" ");
        for (int i = strarr.length - 1; i >= 0; i--){
            if (strarr[i].startsWith(":") && strarr[i].endsWith(":")){

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
            ObjectInputStream in = client.getInput();
            Message incoming;
            boolean isValid = false;
            String username = "";
            BroadcastToClientName(new Message(Message.MSG_HEADER_SUBMIT), client.getName());
            

            while (!isValid) {
                incoming = (Message) in.readObject();
                if (incoming.getHeader() == Message.MSG_HEADER_NAME) {
                    String name = incoming.getMsg();
                    if (name.length() > 0) {
                        Pattern pattern = Pattern.compile("[^\\W]+");
                        Matcher matcher = pattern.matcher(name);

                        if (matcher.matches()) {
                            username = name;
                            isValid = true;

                        }
                    } 
                }
                if (isValid) {
                    for (ClientConnectionData c : clientList) {
                        isValid = !username.equals(c.getUserName()) && isValid;
                        System.out.println(c.getUserName());
                    }
                    if (!isValid) {
                        BroadcastToClientName(new Message(Message.MSG_HEADER_NEWNAME), client.getName());
                    }
                } else {
                    BroadcastToClientName(new Message(Message.MSG_HEADER_RESUBMIT), client.getName());
                }
            }

            client.setUserName(username);
            String namesList = "";

            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getUserName() != null) 
                        if(!c.equals(clientList.get(clientList.size() - 1))){
                            namesList += c.getUserName() + ", ";
                        } else {
                            namesList += c.getUserName() +'\n';
                        }

                }
            }

            Broadcast(new Message(Message.MSG_HEADER_NAMELIST, namesList));
            BroadcastToClientName(new Message(Message.MSG_HEADER_VALID, username), client.getName());
            //notify all that client has joined
            Broadcast(new Message(Message.MSG_HEADER_WELCOME, client.getUserName()));

            while(true) {
                incoming = (Message) in.readObject();
                if (incoming.getHeader() == Message.MSG_HEADER_CHAT) {
                    String chat = incoming.getMsg();
                    if (chat.length() > 0) {
                        BroadcastSkipUserName(new Message(Message.MSG_HEADER_CHAT, emoji(chat), client.getUserName()), client.getUserName());    
                    }
                } else if (incoming.getHeader() == Message.MSG_HEADER_PCHAT) {
                    String chat = incoming.getMsg();
                    ArrayList<String> recipients = incoming.getRecipients();

                    if (chat.length() > 0) {
                        for (String recipient : recipients) {
                            if (client.getUserName().equals(recipient)) 
                                continue;
                            BroadcastToClientUserName(new Message(Message.MSG_HEADER_PCHAT, emoji(chat), client.getUserName()),recipient);
                        }
                    }
                } else if (incoming.getHeader() == Message.MSG_HEADER_QUIT) {
                    break;
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
            Broadcast(new Message(Message.MSG_HEADER_EXIT, client.getUserName()));  
            String namesList = "";

            synchronized (clientList) {
                for (ClientConnectionData c : clientList){
                    if (c.getUserName() != null) 
                        namesList += c.getUserName() + "\n";
                }
            }

            Broadcast(new Message(Message.MSG_HEADER_NAMELIST, namesList));
            try {
                client.getSocket().close();
            } catch (IOException ex) {}

        }
    }
    
}