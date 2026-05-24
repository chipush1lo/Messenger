package org.example.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {

    private Stage primaryStage;
    private NetworkClient networkClient;
    private ChatController chatController;
    private Scene chatScene;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        this.networkClient = new NetworkClient();

        showLoginScreen();
    }

    public void showLoginScreen() throws IOException {
        FXMLLoader loginLoader = new FXMLLoader(ClientApp.class.getResource("/client/login_view.fxml"));
        Scene loginScene = new Scene(loginLoader.load(), 500, 350);
        
        LoginController loginController = loginLoader.getController();
        loginController.setMainApp(this);

        // Preload chat screen to avoid missing messages
        FXMLLoader chatLoader = new FXMLLoader(ClientApp.class.getResource("/client/chat_view.fxml"));
        chatScene = new Scene(chatLoader.load(), 800, 600);
        chatController = chatLoader.getController();
        chatController.setMainApp(this);
        
        primaryStage.setTitle("ICQ Login");
        primaryStage.setScene(loginScene);
        primaryStage.setOnCloseRequest(e -> {
            networkClient.disconnect();
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public void showChatScreen() {
        Platform.runLater(() -> {
            primaryStage.setTitle("ICQ Chat - " + networkClient.getUsername());
            primaryStage.setScene(chatScene);
            chatController.onScreenLoaded();
        });
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public ChatController getChatController() {
        return chatController;
    }

    public static void main(String[] args) {
        launch();
    }
}
