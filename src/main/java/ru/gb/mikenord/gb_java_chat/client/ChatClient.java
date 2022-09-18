package ru.gb.mikenord.gb_java_chat.client;

import javafx.application.Platform;
import ru.gb.mikenord.gb_java_chat.ChatCommand;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.gb.mikenord.gb_java_chat.ChatCommand.*;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ChatController controller;
    private String currentNick;
    private String login;
    private Path msgListFile;

    private boolean isAuthOk = false;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                isAuthOk = waitAuth();
                if (isAuthOk) {
                    controller.getRefStage().setWidth(600);
                    controller.getRefStage().setHeight(400);
                    controller.getClientsList().setMinWidth(200);
                    controller.getClientsList().setVisible(true);
                    msgListFile = Path.of("src", "main", "resources", "ru", "gb", "mikenord"
                            , "gb_java_chat", "client", login + ".msglist");
//                    msgListFile = Path.of(login + ".msglist");
                    if (Files.exists(msgListFile)) {
                        List<String> msgList = Files
                                .lines(msgListFile, StandardCharsets.UTF_8)
                                .collect(Collectors.toList());
                        if (msgList.size() > 0) {
                            int maxLines = Math.min(msgList.size(), 100);
                            Platform.runLater(() -> controller.addMessage(">>>восстановление предыдущих записей>>>"));
                            for (int i = msgList.size() - maxLines; i < msgList.size(); i++) {
                                String tmpLine = msgList.get(i);
                                Platform.runLater(() -> controller.addMessage(tmpLine));
                            }
                            Platform.runLater(() -> controller.addMessage("---начало текущих записей---"));

                        }
                    } else {
                        Files.createFile(msgListFile);
                    }
                    readMessages();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
                Platform.runLater(() -> System.exit(0));
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isAuthOk) {
                controller.getAuthBox().setVisible(false);
                controller.getChatArea().appendText("Тайм-аут!\nАутентификация не выполнена...\n");
                controller.getChatArea().appendText("Работа будет завершена через 5 секунд... ");
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(1000);
                        controller.getChatArea().appendText(">");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                sendMessage(END);
            }
        }).start();
    }

    private boolean waitAuth() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final ChatCommand command = ChatCommand.getCommand(message);
            final String[] params = command.parse(message);
            if (command == END) {
                return false;
            }
            if (command == AUTH_SUCCESS) {
                currentNick = params[0];
                Platform.runLater(() -> controller.setAuth(true));
                Platform.runLater(() -> controller.addMessage(currentNick + " - успешная авторизация"));
                return true;
            } else if (command == ERROR) {
                Platform.runLater(() -> controller.addMessage("Ошибка авторизации: " + params[0]));
            }
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            String message = in.readUTF();
            final ChatCommand command = ChatCommand.getCommand(message);
            final String[] params = command.parse(message);
            if (command == END) {
                Platform.runLater(() -> controller.setAuth(false));
                break;
            }
            if (command == MESSAGE || command == ERROR) {
                Platform.runLater(() -> controller.addMessage(params[0]));
                Files.writeString(msgListFile, params[0] + "\n", StandardOpenOption.APPEND);
            } else if (command == CLIENT_LIST) {
                Platform.runLater(() -> controller.updateClientList(params));
            }
        }
    }

    public void sendMessage(ChatCommand command, String... params) {
        try {
            out.writeUTF(command.collectMessage(params));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentNick() {
        return currentNick;
    }

    public void setNewNick(String newNick) {
        String oldNick = this.currentNick;
        this.currentNick = newNick;
        sendMessage(NICK_CHANGE, oldNick, newNick);
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
