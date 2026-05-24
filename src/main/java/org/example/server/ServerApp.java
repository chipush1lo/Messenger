package org.example.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.server.db.DatabaseManager;

public class ServerApp extends Application {
    
    private ServerController controller;

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.init();
        
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApp.class.getResource("/server/server_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        
        controller = fxmlLoader.getController();
        controller.startServer(8888);
        
        stage.setTitle("ICQ Server");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            controller.stopServer();
            DatabaseManager.close();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
