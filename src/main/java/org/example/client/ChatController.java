package org.example.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.protocol.Message;
import org.example.protocol.MessageType;

public class ChatController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private ListView<String> usersListView;

    @FXML
    private VBox chatContainer;

    @FXML
    private javafx.scene.control.ScrollPane chatScrollPane;

    @FXML
    private VBox replyBox;

    @FXML
    private Label replyContentLabel;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendBtn;

    @FXML
    private Button attachBtn;

    private ClientApp mainApp;
    private String selectedUser = "all";
    private java.util.Map<String, VBox> chatContainers = new java.util.HashMap<>();
    private java.util.Set<String> knownUsers = new java.util.LinkedHashSet<>();
    
    private Message currentReplyTo = null;
    private java.io.File currentAttachment = null;

    public void setMainApp(ClientApp mainApp) {
        this.mainApp = mainApp;
        
        chatContainers.put("all", chatContainer);

        usersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                switchChatContainer(newVal);
            }
        });
    }

    public void onScreenLoaded() {
        currentUserLabel.setText(mainApp.getNetworkClient().getUsername().toUpperCase());
    }

    private void switchChatContainer(String chatName) {
        VBox container = chatContainers.computeIfAbsent(chatName, k -> {
            VBox box = new VBox(10);
            box.setPadding(new javafx.geometry.Insets(15));
            return box;
        });
        chatScrollPane.setContent(container);
    }

    private void refreshUserList(java.util.List<String> onlineUsers) {
        String currentSelection = selectedUser;
        if (onlineUsers != null) {
            for (String u : onlineUsers) {
                if (!u.equals(mainApp.getNetworkClient().getUsername())) {
                    knownUsers.add(u);
                }
            }
        }
        
        usersListView.getItems().clear();
        usersListView.getItems().add("all");
        for (String u : knownUsers) {
            usersListView.getItems().add(u);
        }
        
        if (usersListView.getItems().contains(currentSelection)) {
            usersListView.getSelectionModel().select(currentSelection);
        } else {
            usersListView.getSelectionModel().selectFirst();
        }
    }

    public void handleMessage(Message msg) {
        Platform.runLater(() -> {
            if (msg.getType() == MessageType.USER_LIST) {
                refreshUserList(msg.getUserList());
            } else if (msg.getType() == MessageType.CHAT) {
                addMessageToChat("all", msg);
            } else if (msg.getType() == MessageType.PRIVATE_CHAT) {
                String chatWith = msg.getSender().equals(mainApp.getNetworkClient().getUsername()) ? msg.getReceiver() : msg.getSender();
                if (knownUsers.add(chatWith)) {
                    refreshUserList(null);
                }
                addMessageToChat(chatWith, msg);
            } else if (msg.getType() == MessageType.DELETE_MESSAGE) {
                removeMessageFromUI(msg.getId());
            }
        });
    }

    private void removeMessageFromUI(Long id) {
        if (id == null) return;
        for (VBox container : chatContainers.values()) {
            container.getChildren().removeIf(node -> id.equals(node.getUserData()));
        }
    }

    private void addMessageToChat(String chatName, Message msg) {
        boolean isMe = msg.getSender().equals(mainApp.getNetworkClient().getUsername());
        
        Label nameLabel = new Label(msg.getSender());
        nameLabel.getStyleClass().add("msg-name");

        VBox contentBox = new VBox(5);
        if (isMe) {
            contentBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        } else {
            contentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        }
        
        if (msg.getContent() != null && !msg.getContent().isEmpty()) {
            Label contentLabel = new Label(msg.getContent());
            contentLabel.getStyleClass().add("msg-content");
            contentLabel.setWrapText(true);
            if (isMe) {
                contentLabel.getStyleClass().add("msg-content-me");
            } else {
                contentLabel.getStyleClass().add("msg-content-other");
            }
            contentBox.getChildren().add(contentLabel);
        }
        
        if (msg.getFileName() != null && !msg.getFileName().isEmpty()) {
            Button downloadBtn = new Button("⬇ " + msg.getFileName());
            downloadBtn.getStyleClass().add("file-download-btn");
            downloadBtn.setOnAction(e -> downloadFile(msg));
            contentBox.getChildren().add(downloadBtn);
        }

        VBox msgBox = new VBox();
        msgBox.setUserData(msg.getId());
        
        if (msg.getReplyToContent() != null && !msg.getReplyToContent().isEmpty()) {
            Label quoteLabel = new Label(msg.getReplyToContent());
            quoteLabel.getStyleClass().add("reply-quote");
            quoteLabel.setWrapText(true);
            msgBox.getChildren().add(quoteLabel);
        }
        
        msgBox.getChildren().addAll(nameLabel, contentBox);
        msgBox.getStyleClass().add("msg-box");
        
        if (isMe) {
            msgBox.getStyleClass().add("msg-me");
            nameLabel.setStyle("-fx-alignment: center-right; -fx-pref-width: 100%;");
        } else {
            msgBox.getStyleClass().add("msg-other");
        }
        
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem replyItem = new javafx.scene.control.MenuItem("Відповісти");
        replyItem.setOnAction(e -> {
            currentReplyTo = msg;
            replyContentLabel.setText(msg.getSender() + ": " + (msg.getContent() != null ? msg.getContent() : "[Файл]"));
            replyBox.setVisible(true);
            replyBox.setManaged(true);
        });
        contextMenu.getItems().add(replyItem);
        
        if (isMe && msg.getId() != null) {
            javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("Видалити");
            deleteItem.setOnAction(e -> {
                Message delMsg = new Message(MessageType.DELETE_MESSAGE, mainApp.getNetworkClient().getUsername(), "Server", "");
                delMsg.setId(msg.getId());
                mainApp.getNetworkClient().sendMessage(delMsg);
            });
            contextMenu.getItems().add(deleteItem);
        }
        
        msgBox.setOnContextMenuRequested(e -> {
            contextMenu.show(msgBox, e.getScreenX(), e.getScreenY());
        });

        VBox container = chatContainers.computeIfAbsent(chatName, k -> {
            VBox box = new VBox(10);
            box.setPadding(new javafx.geometry.Insets(15));
            return box;
        });
        container.getChildren().add(msgBox);
    }

    @FXML
    public void onSendAction(ActionEvent event) {
        String content = messageField.getText().trim();
        if (content.isEmpty() && currentAttachment == null) return;

        MessageType type = selectedUser.equals("all") ? MessageType.CHAT : MessageType.PRIVATE_CHAT;
        Message msg = new Message(type, mainApp.getNetworkClient().getUsername(), selectedUser, content);
        
        if (currentReplyTo != null) {
            msg.setReplyToId(currentReplyTo.getId());
            msg.setReplyToContent(currentReplyTo.getSender() + ": " + (currentReplyTo.getContent() != null ? currentReplyTo.getContent() : "[Файл]"));
        }
        
        if (currentAttachment != null) {
            try {
                byte[] fileBytes = java.nio.file.Files.readAllBytes(currentAttachment.toPath());
                msg.setFileName(currentAttachment.getName());
                msg.setFileDataBase64(java.util.Base64.getEncoder().encodeToString(fileBytes));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        mainApp.getNetworkClient().sendMessage(msg);
        messageField.clear();
        onCancelReply(null);
        currentAttachment = null;
        attachBtn.setText("📎");
    }

    @FXML
    public void onCancelReply(ActionEvent event) {
        currentReplyTo = null;
        replyBox.setVisible(false);
        replyBox.setManaged(false);
    }

    @FXML
    public void onAttachAction(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        java.io.File file = fileChooser.showOpenDialog(messageField.getScene().getWindow());
        if (file != null) {
            if (file.length() > 5 * 1024 * 1024) { // 5MB limit
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("File too large");
                alert.setHeaderText(null);
                alert.setContentText("Please select a file smaller than 5 MB.");
                alert.showAndWait();
                return;
            }
            currentAttachment = file;
            attachBtn.setText("📎 (1)");
        }
    }

    private void downloadFile(Message msg) {
        if (msg.getFileDataBase64() == null) return;
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setInitialFileName(msg.getFileName());
        java.io.File file = fileChooser.showSaveDialog(messageField.getScene().getWindow());
        if (file != null) {
            try {
                byte[] fileBytes = java.util.Base64.getDecoder().decode(msg.getFileDataBase64());
                java.nio.file.Files.write(file.toPath(), fileBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
