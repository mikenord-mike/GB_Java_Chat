package ru.gb.mikenord.gb_java_chat.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import static ru.gb.mikenord.gb_java_chat.ChatCommand.*;

public class ChatController {
    @FXML
    private ListView<String> clientsList;
    @FXML
    private HBox authBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    public Button nickChangeButton;
    @FXML
    private HBox messageBox;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField chatMsgField;

    private final ChatClient client;
    private Stage refStage;
    private String selectedNick;

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
        if (selectedNick != null) {
            client.sendMessage(PRIVATE_MESSAGE, selectedNick, message);
            selectedNick = null;
        } else {
            client.sendMessage(MESSAGE, message);
        }
        chatMsgField.clear();
        chatMsgField.requestFocus();
    }

    public void addMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    public void setAuth(boolean success) {
        authBox.setVisible(!success);
        nickChangeButton.setVisible(success);
        messageBox.setVisible(success);
        clientsList.setVisible(success);
    }

    public void signInBtnClick() {
        client.setLogin(loginField.getText());
        client.sendMessage(AUTH_REQUEST, loginField.getText(), passField.getText());
    }

    public HBox getAuthBox() {
        return authBox;
    }

    public TextArea getChatArea() {
        return chatArea;
    }

    public ChatClient getClient() {
        return client;
    }

    public Stage getRefStage() {
        return refStage;
    }

    public void setRefStage(Stage refStage) {
        this.refStage = refStage;
    }

    public ListView<String> getClientsList() {
        return clientsList;
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            final String selectedNick = clientsList.getSelectionModel().getSelectedItem();
            if (selectedNick != null && !selectedNick.isEmpty()) {
                this.selectedNick = selectedNick;
            }
        }
    }

    public void updateClientList(String[] clients) {
        clientsList.getItems().clear();
        clientsList.getItems().addAll(clients);
    }

    public void nickChangeBtnClick(ActionEvent actionEvent) {

        TextInputDialog dialog = new TextInputDialog(client.getCurrentNick());

        dialog.setTitle("Смена ника");
        dialog.setHeaderText("Введите новый ник");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String name = result.get();
            if (name.isBlank()) {
                return;
            }
            if (clientsList.getItems().stream().anyMatch(name::equals)) {
                chatArea.appendText( "Такой ник уже зарегистрирован в системе\n");
                return;
            }
            client.setNewNick(name);
        }
    }
}