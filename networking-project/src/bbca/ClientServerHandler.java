package bbca;

import java.io.ObjectInputStream;


public class ClientServerHandler implements Runnable {
    private static ObjectInputStream socketIn;
    public static int status = 0; //0 if user not active, 1 if user is being validated, 2 if user is active
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
                switch (incoming.getHeader()) {
                    case (Message.MSG_HEADER_SUBMIT):
                        System.out.println("Welcome to the BCA Chat App! Please enter a username:");
                        status = 1;
                        break;
                    case (Message.MSG_HEADER_RESUBMIT):
                        System.out.println("Invalid username! Username contains a non-word character. Enter a different username:");
                        break;
                    case (Message.MSG_HEADER_NEWNAME):
                        System.out.println("Username taken! Enter a different username:");
                        break;
                    case (Message.MSG_HEADER_VALID):
                        
                        System.out.println("List of chat members:");
                        System.out.println(ClientServerHandler.namesList);
                        status = 2;
                        break;
                    case (Message.MSG_HEADER_WELCOME):
                        String name = incoming.getMsg().trim();
                        System.out.println(String.format("%s has joined.", name));
                        break;
                    case (Message.MSG_HEADER_NAMELIST):  
                        namesList = incoming.getMsg().trim();
                        break;
                    case (Message.MSG_HEADER_CHAT):
                        String text_chat = incoming.getMsg().trim();
                        String sender_chat = incoming.getSender().trim();
                        System.out.println(String.format("%s: %s", sender_chat, text_chat));
                        break;
                    case (Message.MSG_HEADER_PCHAT):
                        String text_pchat = incoming.getMsg().trim();
                        String sender_pchat = incoming.getSender().trim();
                        System.out.println(String.format("%s [private]: %s", sender_pchat, text_pchat));
                        break;
                    case (Message.MSG_HEADER_EXIT):
                        String name_exit = incoming.getMsg().trim();
                        System.out.println(String.format("%s has left the chat.", name_exit)); 
                        break;
                    default:
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