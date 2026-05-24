package org.example.client;

import org.example.protocol.Message;
import org.example.protocol.XmlProtocolHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread listenThread;
    private Consumer<Message> onMessageReceived;
    private Consumer<Exception> onError;
    private String username;

    public void connect(String ip, int port, String username, Consumer<Message> onMessageReceived, Consumer<Exception> onError) throws IOException {
        this.username = username;
        this.onMessageReceived = onMessageReceived;
        this.onError = onError;
        
        socket = new Socket(ip, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        listenThread = new Thread(this::listenForMessages);
        listenThread.setDaemon(true);
        listenThread.start();
    }

    private void listenForMessages() {
        try {
            while (!socket.isClosed()) {
                int length = in.readInt();
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                String xml = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                Message msg = XmlProtocolHelper.fromXml(xml);
                if (onMessageReceived != null) {
                    onMessageReceived.accept(msg);
                }
            }
        } catch (Exception e) {
            if (onError != null) {
                onError.accept(e);
            }
        }
    }

    public void sendMessage(Message msg) {
        new Thread(() -> {
            try {
                String xml = org.example.protocol.XmlProtocolHelper.toXml(msg);
                byte[] bytes = xml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                synchronized (out) {
                    out.writeInt(bytes.length);
                    out.write(bytes);
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Send Error");
                    alert.setHeaderText("Failed to send message");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getUsername() {
        return username;
    }
}
