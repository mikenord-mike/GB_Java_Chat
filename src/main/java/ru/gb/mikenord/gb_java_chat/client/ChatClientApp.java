package ru.gb.mikenord.gb_java_chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import static ru.gb.mikenord.gb_java_chat.ChatCommand.END;

public class ChatClientApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApp.class.getResource("client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450 , 300);   // 600-400
        stage.setResizable(false);
        stage.setTitle("Mikenord Chat client");
        stage.setScene(scene);
        stage.show();

        //  window closing event handling
        ChatController controller = fxmlLoader.getController();
        controller.setRefStage(stage);
        stage.setOnCloseRequest(event -> Platform.runLater(()-> controller.getClient().sendMessage(END)));
    }

     public static void main(String[] args) {
        launch();
    }
}
