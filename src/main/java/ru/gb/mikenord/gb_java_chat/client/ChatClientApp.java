package ru.gb.mikenord.gb_java_chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatClientApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApp.class.getResource("client-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 400, 240);
        stage.setTitle("Mikenord Chat client");
        stage.setScene(scene);
        stage.show();

        //  window closing event handling
        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> controller.getClient().sendMessage("/end"));
    }

    public static void main(String[] args) {
        launch();
    }
}
