package org.example.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.protocol.Message;
import org.example.protocol.MessageType;

public class LoginController {

    @FXML
    private TextField ipField;

    @FXML
    private TextField usernameField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button nextBtn;

    private ClientApp mainApp;

    public void setMainApp(ClientApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void onNextAction(ActionEvent event) {
        String ip = ipField.getText().trim();
        String username = usernameField.getText().trim();

        if (ip.isEmpty() || username.isEmpty()) {
            errorLabel.setText("Please enter IP and Username");
            return;
        }

        nextBtn.setDisable(true);
        errorLabel.setText("Connecting...");

        new Thread(() -> {
            try {
                // Try connecting
                mainApp.getNetworkClient().connect(ip, 8888, username, this::onMessageReceived, this::onError);
                
                // Send AUTH message
                Message authMsg = new Message(MessageType.AUTH, username, "Server", "Login request");
                mainApp.getNetworkClient().sendMessage(authMsg);

            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Connection failed: " + e.getMessage());
                    nextBtn.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    public void onCancelAction(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    private void onMessageReceived(Message msg) {
        if (msg.getType() == MessageType.AUTH_SUCCESS) {
            Platform.runLater(() -> {
                mainApp.showChatScreen();
            });
        } else if (msg.getType() == MessageType.AUTH_ERROR) {
            Platform.runLater(() -> {
                errorLabel.setText(msg.getContent());
                nextBtn.setDisable(false);
            });
        } else {
            // If we receive other messages while logging in, maybe forward them
            if (mainApp.getChatController() != null) {
                mainApp.getChatController().handleMessage(msg);
            }
        }
    }

    private void onError(Exception e) {
        Platform.runLater(() -> {
            errorLabel.setText("Network error: " + e.getMessage());
            nextBtn.setDisable(false);
        });
    }
}
