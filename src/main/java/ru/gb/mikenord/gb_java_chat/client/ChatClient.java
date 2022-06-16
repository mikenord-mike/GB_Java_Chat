package ru.gb.mikenord.gb_java_chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                waitAuth();
                readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
                System.exit(0);
            }
        }).start();
    }

    private void waitAuth() throws IOException {
        while (true) {
            final String message = in.readUTF();
            if (message.startsWith("/auth_ok")) {
                String[] split = message.split("\\p{Blank}+");
                String nick = split[1];
                controller.addMessage("Успешная авторизация под ником " + nick);
                controller.setAuth(true);
                break;
            } else if (message.startsWith("/user_already_logged")) {
                controller.addMessage("Пользователь уже авторизован");
            } else if (message.startsWith("/invalid_login_and_pass")) {
                controller.addMessage("Неверные логин и пароль");
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
            if ("/end".equals(message)) {
                controller.setAuth(false);
                break;
            }
            if (message.startsWith("/w")) {
                final String[] split = message.split("/w\\p{Blank}*");
                final String senderNick = split[1];
                final String privateMessage = split[2];
                controller.addMessage("Сообщение от '" + senderNick + "' :" + privateMessage);
            } else {
                controller.addMessage(message);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
