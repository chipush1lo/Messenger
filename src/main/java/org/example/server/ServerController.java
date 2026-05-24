package org.example.server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.example.protocol.Message;
import org.example.protocol.MessageType;
import org.example.server.db.DatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerController {

    @FXML
    private TextArea logArea;

    @FXML
    private ListView<String> usersListView; // We will use this for conversations now

    private Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private Thread serverThread;
    
    private String selectedConversation = "Global Chat";
    private List<Message> allMessages = new ArrayList<>();

    public void startServer(int port) {
        // Load history
        allMessages.addAll(DatabaseManager.getAllMessages());
        
        usersListView.getItems().add("Global Chat");
        updateConversationsList();
        
        usersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedConversation = newV;
                refreshLogArea();
            }
        });
        usersListView.getSelectionModel().selectFirst();

        logMessageSystem("Starting server on port " + port + "...");
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                logMessageSystem("Server started successfully.");
                
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    logMessageSystem("New client connected: " + socket.getInetAddress());
                    
                    ClientHandler handler = new ClientHandler(socket, this);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    Platform.runLater(() -> logArea.appendText("Server error: " + e.getMessage() + "\n"));
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addClient(String username, ClientHandler handler) {
        activeClients.put(username, handler);
        logMessageSystem("User authenticated: " + username);
    }

    public synchronized void removeClient(String username) {
        activeClients.remove(username);
        logMessageSystem("User disconnected: " + username);
    }

    public void broadcastUserList() {
        List<String> users = new ArrayList<>(activeClients.keySet());
        Message msg = new Message(MessageType.USER_LIST, "Server", "all", null);
        msg.setUserList(users);
        broadcastMessage(msg);
    }

    public void broadcastMessage(Message msg) {
        for (ClientHandler handler : activeClients.values()) {
            handler.sendMessage(msg);
        }
    }

    public void sendPrivateMessage(Message msg) {
        ClientHandler receiver = activeClients.get(msg.getReceiver());
        if (receiver != null) {
            receiver.sendMessage(msg);
        }
        
        // Also send back to sender so they see it
        ClientHandler sender = activeClients.get(msg.getSender());
        if (sender != null && !msg.getSender().equals(msg.getReceiver())) {
            sender.sendMessage(msg);
        }
    }

    public void logMessage(Message msg) {
        Platform.runLater(() -> {
            allMessages.add(msg);
            updateConversationsList();
            refreshLogArea();
        });
    }

    public void removeMessage(Long id) {
        Platform.runLater(() -> {
            allMessages.removeIf(m -> id.equals(m.getId()));
            updateConversationsList();
            refreshLogArea();
        });
    }

    private void logMessageSystem(String text) {
        Platform.runLater(() -> {
            if (selectedConversation.equals("Global Chat")) {
                logArea.appendText(text + "\n");
            }
        });
    }

    private void updateConversationsList() {
        Platform.runLater(() -> {
            String currentSelection = selectedConversation;
            List<String> convos = new ArrayList<>();
            convos.add("Global Chat");
            
            for (Message msg : allMessages) {
                if (msg.getType() == MessageType.PRIVATE_CHAT) {
                    String convoName = getConversationName(msg.getSender(), msg.getReceiver());
                    if (!convos.contains(convoName)) {
                        convos.add(convoName);
                    }
                }
            }
            
            usersListView.getItems().setAll(convos);
            if (convos.contains(currentSelection)) {
                usersListView.getSelectionModel().select(currentSelection);
            }
        });
    }

    private void refreshLogArea() {
        Platform.runLater(() -> {
            logArea.clear();
            for (Message msg : allMessages) {
                String text = msg.getContent() != null && !msg.getContent().isEmpty() ? msg.getContent() : "";
                if (msg.getFileName() != null && !msg.getFileName().isEmpty()) {
                    text += (text.isEmpty() ? "" : " ") + "[FILE: " + msg.getFileName() + "]";
                }
                
                if (selectedConversation.equals("Global Chat")) {
                    if (msg.getType() == MessageType.CHAT) {
                        logArea.appendText(msg.getTimestamp() + " | " + msg.getSender() + ": " + text + "\n");
                    }
                } else {
                    if (msg.getType() == MessageType.PRIVATE_CHAT) {
                        String convoName = getConversationName(msg.getSender(), msg.getReceiver());
                        if (convoName.equals(selectedConversation)) {
                            logArea.appendText(msg.getTimestamp() + " | " + msg.getSender() + ": " + text + "\n");
                        }
                    }
                }
            }
        });
    }

    private String getConversationName(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + " <---> " + user2;
        } else {
            return user2 + " <---> " + user1;
        }
    }
}
