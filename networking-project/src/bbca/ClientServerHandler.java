package bbca;

import java.io.ObjectInputStream;


public class ClientServerHandler implements Runnable {
    private static ObjectInputStream socketIn;
    public static int state = 0; //0: user not active, 1: user's name being validated 2: user is active
    public static String namesList = "";

    public ClientServerHandler(ObjectInputStream socketIn) {
        this.socketIn = socketIn;
    }

    @Override
    public void run() {
        try {
            Message incoming;

            while(true) {
                incoming = (Message) socketIn.readObject();
                if (incoming.getHeader() == Message.MSG_HEADER_SUBMIT) {
                    state = 1;
                    System.out.println("Welcome to the BCA Chat App! Please enter a username:");
                } else if (incoming.getHeader() == Message.MSG_HEADER_RESUBMIT) {
                    System.out.println("Invalid username! Username contains a non-word character. Enter a different username:");
                } else if (incoming.getHeader() == Message.MSG_HEADER_NEWNAME) {
                    System.out.println("Username taken! Enter a different username:");
                } else if (incoming.getHeader() == Message.MSG_HEADER_VALID) {
                    state = 2;
                    System.out.println("List of chat members:");
                    System.out.println(ClientServerHandler.namesList);
                } else if (incoming.getHeader() == Message.MSG_HEADER_WELCOME) {
                    String name = incoming.getMsg().trim();
                    System.out.println(String.format("%s has joined.", name));
                } else if (incoming.getHeader() == Message.MSG_HEADER_NAMELIST) {
                    namesList = incoming.getMsg().trim();
                } else if (incoming.getHeader() == Message.MSG_HEADER_CHAT) {
                    String text = incoming.getMsg().trim();
                    String sender = incoming.getSender().trim();
                    System.out.println(String.format("%s: %s", sender, text));
                } else if (incoming.getHeader() == Message.MSG_HEADER_PCHAT) {
                    String text = incoming.getMsg().trim();
                    String sender = incoming.getSender().trim();
                    System.out.println(String.format("%s [private]: %s", sender, text));
                } else if (incoming.getHeader() == Message.MSG_HEADER_EXIT) {
                    String name = incoming.getMsg().trim();
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