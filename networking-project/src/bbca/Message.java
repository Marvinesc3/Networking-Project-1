package bbca;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    public static final long serialVersionUID = 7L;

    public static final int MSG_HEADER_SUBMIT = 0;
    public static final int MSG_HEADER_RESUBMIT = 1;
    public static final int MSG_HEADER_NEWNAME = 2;
    public static final int MSG_HEADER_CONFIRM = 3;
    public static final int MSG_HEADER_NAME = 4;
    public static final int MSG_HEADER_WELCOME = 5;
    public static final int MSG_HEADER_EXIT = 6;
    public static final int MSG_HEADER_CHAT = 7;
    public static final int MSG_HEADER_PCHAT = 8;
    public static final int MSG_HEADER_QUIT = 9;
    public static final int MSG_HEADER_NAMELIST = 10;

    private int header;
    private String sender;
    private String msg;
    private ArrayList<String> recipients;

    public Message(int header) {
        this.header = header;
    }

    public Message(int header, String msg) {
        this.header = header;
        this.msg = msg;
    }

    public Message(int header, String msg, ArrayList<String> recipients) {
        this.header = header;
        this.msg = msg;
        this.recipients = recipients;
    }

    public Message(int header, String msg, String sender) {
        this.header = header;
        this.msg = msg;
        this.sender = sender;
    }
    
    public String getMsg() {
            return msg;
    }

    public int getHeader() {
        return header;
    }
    
    public String getSender() {
        return sender;
    }

    public ArrayList<String> getRecipients() {
            return recipients;
        }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public void setHeader(int header) {
        this.header = header;
    }

    public void setSender(String sender) {
        this.sender = sender;
    } 
    
    public void setRecipients(ArrayList<String> recipients) {
        this.recipients = recipients;
    }

    
}