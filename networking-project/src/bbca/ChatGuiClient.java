package bbca;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import javax.swing.*;

/**
 * For Java 8, javafx is installed with the JRE. You can run this program normally.
 * For Java 9+, you must install JavaFX separately: https://openjfx.io/openjfx-docs/
 * If you set up an environment variable called PATH_TO_FX where JavaFX is installed
 * you can compile this program with:
 *  Mac/Linux:
 *      > javac --module-path $PATH_TO_FX --add-modules javafx.controls day10_chatgui/ChatGuiClient.java
 *  Windows CMD:
 *      > javac --module-path %PATH_TO_FX% --add-modules javafx.controls day10_chatgui/ChatGuiClient.java
 *  Windows Powershell:
 *      > javac --module-path $env:PATH_TO_FX --add-modules javafx.controls day10_chatgui/ChatGuiClient.java
 *
 * Then, run with:
 *
 *  Mac/Linux:
 *      > java --module-path $PATH_TO_FX --add-modules javafx.controls day10_chatgui.ChatGuiClient
 *  Windows CMD:
 *      > java --module-path %PATH_TO_FX% --add-modules javafx.controls day10_chatgui.ChatGuiClient
 *  Windows Powershell:
 *      > java --module-path $env:PATH_TO_FX --add-modules javafx.controls day10_chatgui.ChatGuiClient
 *
 * There are ways to add JavaFX to your to your IDE so the compile and run process is streamlined.
 * That process is a little messy for VSCode; it is easiest to do it via the command line there.
 * However, you should open  Explorer -> Java Projects and add to Referenced Libraries the javafx .jar files
 * to have the syntax coloring and autocomplete work for JavaFX
 */

class ServerInfo {
    public final String serverAddress;
    public final int serverPort;

    public ServerInfo(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
}

public class ChatGuiClient extends Application {
    String serverip;
    int port;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    String namesList = "";
    private Stage stage;
    private TextArea messageArea;
    private TextField textInput;
    private Button sendButton;
    private Button funButton;

    ServerListener socketListener;
    private ServerInfo serverInfo;



    //    private ServerInfo serverInfo;
    //volatile keyword makes individual reads/writes of the variable atomic
    // Since username is accessed from multiple threads, atomicity is important
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {

        List<String> args = getParameters().getUnnamed();
        if (args.size() == 2){
            this.serverInfo = new ServerInfo(args.get(0), Integer.parseInt(args.get(1)));
        }
        else {
            //otherwise, use a Dialog.
            Optional<ServerInfo> info = getServerIpAndPort();
            if (info.isPresent()) {
                this.serverInfo = info.get();
            }
            else{
                Platform.exit();
                return;
            }
        }

        this.stage = primaryStage;
        BorderPane borderPane = new BorderPane();

        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        borderPane.setCenter(messageArea);

        //At first, can't send messages - wait for WELCOME!
        textInput = new TextField();
        textInput.setOnAction(e -> sendMessage());
        sendButton = new Button("Send");
        funButton = new Button("Emoji List");
        sendButton.setDisable(true);
        funButton.setDisable(true);
        sendButton.setOnAction(e -> sendMessage());
        funButton.setOnAction(e -> sendFun());

        HBox hbox = new HBox();
        hbox.getChildren().addAll(new Label("Message: "), textInput, sendButton, funButton);
        HBox.setHgrow(textInput, Priority.ALWAYS);
        borderPane.setBottom(hbox);

        Scene scene = new Scene(borderPane, 400, 500);
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();

        socketListener = new ServerListener();

        //Handle GUI closed event
        stage.setOnCloseRequest(e -> {
            socketListener.appRunning = false;
            try {
                out.writeObject(new Message(Message.MSG_HEADER_QUIT));

                socket.close();
            } catch (IOException ex) {}
        });

        new Thread(socketListener).start();
    }

    private void sendFun() {
        ArrayList<String> emojis = new ArrayList<>(Arrays.asList (":happy:", "😃", ":sad:", "😞",
                ":angry:", "😠", ":crying:", "😭", ":lol:", "😂", ":love:", "😍", ":fire:", "🔥",
                ":wink:", "😉", ":kiss:", "😘", ":crazy:", "🤪	", ":money:", "🤑", ":shush:", "🤫", ":think:", "🤔",
                ":meh:", "😐", ":gross:", "🤢", ":hot:", "🥵", ":party:", "🥳", ":poggers:", "😲", ":100:", "💯",
                ":hands:", "🙏", ":strong:", "💪", ":eyes:", "👀", ":cap:", "🧢"));
        textInput.clear();
        try {
            Platform.runLater(() -> {
                messageArea.appendText(String.format("Emoji List: %s\n", emojis));
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage(){
        String line = textInput.getText().trim();
        if (line.length()  == 0)
            return;
        textInput.clear();

        try {
            if (socketListener.status == 2) {
                if (line.startsWith("@")) {
                    boolean mentioned = true;
                    ArrayList<String> names = new ArrayList<String>();
                    String msg = "";

                    String[] words = line.split(" ");

                    for (int i = 0; i < words.length; i++) {
                        if (mentioned) {
                            if (words[i].startsWith("@")) {
                                names.add(words[i].substring(1));
                            } else {
                                mentioned = false;
                                msg += words[i] + " ";
                            }
                        } else {
                            msg += words[i] + " ";
                        }
                    }

                    if (msg.length() > 0) {
                        out.writeObject(new Message(Message.MSG_HEADER_PCHAT, msg, names));
                    }

                    Platform.runLater(() -> {
                        messageArea.appendText("You: " + line + "\n");
                    });
                }
                else if (line.toLowerCase().startsWith("/whoishere")) {
                    Platform.runLater(() -> {
                        messageArea.appendText("List of chat members:\n");
                        messageArea.appendText(namesList + "\n");
                    });
                }
                else {
                    out.writeObject(new Message(Message.MSG_HEADER_CHAT, line));
                    Platform.runLater(() -> {
                        messageArea.appendText("You: " + line + "\n");
                    });
                }
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Optional<ServerInfo> getServerIpAndPort() {
        // In a more polished product, we probably would have the ip /port hardcoded
        // But this a great way to demonstrate making a custom dialog
        // Based on Custom Login Dialog from https://code.makery.ch/blog/javafx-dialogs-official/

        // Create a custom dialog for server ip / port
        Dialog<ServerInfo> getServerDialog = new Dialog<>();
        getServerDialog.setTitle("Enter Server Info");
        getServerDialog.setHeaderText("Enter your server's IP address and port: ");

        // Set the button types.
        ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
        getServerDialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // Create the ip and port labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField ipAddress = new TextField();
        ipAddress.setPromptText("e.g. localhost, 127.0.0.1");
        grid.add(new Label("IP Address:"), 0, 0);
        grid.add(ipAddress, 1, 0);

        TextField port = new TextField();
        port.setPromptText("e.g. 54321");
        grid.add(new Label("Port number:"), 0, 1);
        grid.add(port, 1, 1);

        // Enable/Disable connect button depending on whether a address/port was entered.
        Node connectButton = getServerDialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        ipAddress.textProperty().addListener((observable, oldValue, newValue) -> {
            connectButton.setDisable(newValue.trim().isEmpty());
        });

        port.textProperty().addListener((observable, oldValue, newValue) -> {
            // Only allow numeric values
            if (! newValue.matches("\\d*"))
                port.setText(newValue.replaceAll("[^\\d]", ""));

            connectButton.setDisable(newValue.trim().isEmpty());
        });

        getServerDialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(ipAddress::requestFocus);


        // Convert the result to a ServerInfo object when the login button is clicked.
        getServerDialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return new ServerInfo(ipAddress.getText(), Integer.parseInt(port.getText()));
            }
            return null;
        });

        return getServerDialog.showAndWait();
    }

    private String getName(){
        String username = "";
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Enter Chat Name");
        nameDialog.setHeaderText("Please enter your username.");
        nameDialog.setContentText("Name: ");

        while(username.equals("")) {
            Optional<String> name = nameDialog.showAndWait();
            username = name.get().trim();
        }
        return username;
    }

    class ServerListener implements Runnable {
        volatile boolean appRunning = false;
        public int status = 0;

        public void run() {

            try {
                socket = new Socket(serverInfo.serverAddress, serverInfo.serverPort);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                appRunning = true;
                while (appRunning) {
                    Message incoming = (Message) in.readObject();
                    if (incoming.getHeader() == Message.MSG_HEADER_SUBMIT) {
                        status = 1;
                        Platform.runLater(() -> {
                            try {
                                out.writeObject(new Message(Message.MSG_HEADER_NAME, getName()));
                            } catch (IOException ex) {}
                        });
                    }
                     else if (incoming.getHeader() == Message.MSG_HEADER_RESUBMIT) {
                        Platform.runLater(() -> {
                            try {
                                messageArea.appendText("Invalid username! Username contains a non-word character. Enter a different username:\n");
                                out.writeObject(new Message(Message.MSG_HEADER_NAME, getName()));
                            } catch (IOException ex) {}
                        });
                    } else if (incoming.getHeader() == Message.MSG_HEADER_NEWNAME) {
                        Platform.runLater(() -> {
                            try {
                                messageArea.appendText("Username is already in use!. Enter a different username:\n");
                                out.writeObject(new Message(Message.MSG_HEADER_NAME, getName()));
                            } catch (IOException ex) {}
                            messageArea.appendText("Username is already in use!. Enter a different username:\n");
                        });
                    }
                    else if (incoming.getHeader() == Message.MSG_HEADER_VALID) {
                        status = 2;
                        Platform.runLater(() -> {
                            sendButton.setDisable(false);
                            funButton.setDisable(false);
                        });

                    }
                    else if (incoming.getHeader() == Message.MSG_HEADER_WELCOME) {
                        String name = incoming.getMsg().trim();
                        Platform.runLater(() -> {
                            messageArea.appendText(String.format("%s has joined.\n", name));
                        });
                    }
                    else if (incoming.getHeader() == Message.MSG_HEADER_NAMELIST) {
                        Platform.runLater(() -> {
                            namesList = incoming.getMsg().trim();
                            messageArea.appendText(String.format("List of chat members: %s\n", namesList));
                        });
                    }
                    else if (incoming.getHeader() == Message.MSG_HEADER_CHAT) {
                        String text = incoming.getMsg().trim();
                        String sender = incoming.getSender().trim();
                        Platform.runLater(() -> {
                            messageArea.appendText(String.format("%s: %s\n", sender, text));
                        });
                    } else if (incoming.getHeader() == Message.MSG_HEADER_PCHAT) {
                        String text = incoming.getMsg().trim();
                        String sender = incoming.getSender().trim();
                        Platform.runLater(() -> {
                            messageArea.appendText(String.format("%s [private]: %s\n", sender, text));
                        });
                    } else if (incoming.getHeader() == Message.MSG_HEADER_EXIT) {
                        String name = incoming.getMsg().trim();
                        Platform.runLater(() -> {
                            messageArea.appendText(String.format("%s has left the chat.\n", name));
                        });
                    } else {
                        Platform.runLater(() -> {
                            messageArea.appendText("" + incoming.getHeader() + "\n");
                        });}
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (Exception e) {
                if (appRunning)
                    e.printStackTrace();
            }
            finally {
                Platform.runLater(() -> {
                    stage.close();
                });
                try {
                    if (socket != null)
                        socket.close();
                }
                catch (IOException e){
                }
            }
        }
    }
}