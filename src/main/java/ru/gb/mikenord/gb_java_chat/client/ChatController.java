package ru.gb.mikenord.gb_java_chat.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.util.Optional;

public class ChatController {
    @FXML
    private HBox authBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private HBox messageBox;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField chatMsgField;

    private final ChatClient client;

    public ChatController() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                showNotification();
            }
        }
    }

    private void showNotification() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключиться к серверу.\n" +
                        "Проверьте, что сервер запущен и доступен",
                new ButtonType("Попробовать снова", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Ошибка подключения");
        Optional<ButtonType> answer = alert.showAndWait();
        Boolean isExit = answer
                .map(select -> select.getButtonData().isCancelButton())
                .orElse(false);
        if (isExit) {
            System.exit(0);
        }
    }

    public void clickSendButton() {
        String message = chatMsgField.getText();
        if (message.isBlank()) {
            return;
        }
        client.sendMessage(message);
        chatMsgField.clear();
        chatMsgField.requestFocus();
    }

    public void addMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    public void setAuth(boolean success) {
        authBox.setVisible(!success);
        messageBox.setVisible(success);
    }

    public void signInBtnClick() {
        client.sendMessage("/auth " + loginField.getText() + " " + passField.getText());
    }

    public ChatClient getClient() {
        return client;
    }
}