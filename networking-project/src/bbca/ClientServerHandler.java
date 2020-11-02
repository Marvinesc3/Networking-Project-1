package bbca;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Random;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
                if (incoming.getHeader() == Message.HEADER_SUBMIT) {
                    state = 1;
                    System.out.println("Welcome to the BCA Chat App! Please enter a username:");
                } else if (incoming.getHeader() == Message.HEADER_RESUBMIT) {
                    System.out.println("Invalid username! Username contains a non-word character. Enter a different username:");
                } else if (incoming.getHeader() == Message.HEADER_NEWNAME) {
                    System.out.println("Username is already in use!. Enter a different username:");
                } else if (incoming.getHeader() == Message.HEADER_CONFIRM) {
                    state = 2;
                    System.out.println("List of chat members:");
                    System.out.println(ClientServerHandler.namesList);
                } else if (incoming.getHeader() == Message.HEADER_WELCOME) {
                    String name = incoming.getMsg().trim();
                    System.out.println(String.format("%s has joined.", name));
                } else if (incoming.getHeader() == Message.HEADER_NAMELIST) {
                    namesList = incoming.getMsg().trim();
                } else if (incoming.getHeader() == Message.HEADER_CHAT) {
                    String text = incoming.getMsg().trim();
                    String sender = incoming.getSender().trim();
                    System.out.println(String.format("%s: %s", sender, text));
                } else if (incoming.getHeader() == Message.HEADER_PCHAT) {
                    String text = incoming.getMsg().trim();
                    String sender = incoming.getSender().trim();
                    System.out.println(String.format("%s [private]: %s", sender, text));
                } else if (incoming.getHeader() == Message.HEADER_EXIT) {
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