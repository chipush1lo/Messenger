package org.example.server;

import org.example.protocol.Message;
import org.example.protocol.MessageType;
import org.example.protocol.XmlProtocolHelper;
import org.example.server.db.DatabaseManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ServerController serverController;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public ClientHandler(Socket socket, ServerController serverController) {
        this.socket = socket;
        this.serverController = serverController;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while (!socket.isClosed()) {
                int length = in.readInt();
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                String xmlInput = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                Message msg = XmlProtocolHelper.fromXml(xmlInput);

                switch (msg.getType()) {
                    case AUTH:
                        handleAuth(msg);
                        break;
                    case CHAT:
                    case PRIVATE_CHAT:
                        handleChat(msg);
                        break;
                    case DELETE_MESSAGE:
                        handleDelete(msg);
                        break;
                    case DISCONNECT:
                        handleDisconnect();
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            if (username != null) {
                serverController.removeClient(username);
                serverController.broadcastUserList();
            }
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            serverController.logMessage(new Message(MessageType.CHAT, "System", "all", "Error in client " + username + ": " + e.getMessage() + "\n" + sw.toString()));
        } finally {  handleDisconnect();
        }
    }

    private void handleAuth(Message msg) throws IOException {
        this.username = msg.getSender();
        serverController.addClient(this.username, this);
        
        // Send success
        Message response = new Message(MessageType.AUTH_SUCCESS, "Server", this.username, "Authenticated successfully");
        sendMessage(response);

        // Broadcast updated user list
        serverController.broadcastUserList();
        
        // Send chat history
        List<Message> history = DatabaseManager.getAllMessages();
        for (Message hMsg : history) {
            // Only send public messages or messages where this user is sender/receiver
            if (hMsg.getType() == MessageType.CHAT || 
               (hMsg.getType() == MessageType.PRIVATE_CHAT && (hMsg.getSender().equals(username) || hMsg.getReceiver().equals(username)))) {
                sendMessage(hMsg);
            }
        }
    }

    private void handleChat(Message msg) {
        DatabaseManager.saveMessage(msg);
        serverController.logMessage(msg);

        if (msg.getType() == MessageType.CHAT) {
            serverController.broadcastMessage(msg);
        } else if (msg.getType() == MessageType.PRIVATE_CHAT) {
            serverController.sendPrivateMessage(msg);
        }
    }

    private void handleDelete(Message msg) {
        DatabaseManager.deleteMessageById(msg.getId());
        serverController.removeMessage(msg.getId());
        // Broadcast the delete command to everyone
        serverController.broadcastMessage(msg);
    }

    private void handleDisconnect() {
        if (username != null) {
            serverController.removeClient(username);
            serverController.broadcastUserList();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message msg) {
        try {
            String xml = XmlProtocolHelper.toXml(msg);
            byte[] bytes = xml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            synchronized(out) {
                out.writeInt(bytes.length);
                out.write(bytes);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}
