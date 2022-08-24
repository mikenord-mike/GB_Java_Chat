package ru.gb.mikenord.gb_java_chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Ожидаю подключения...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Новый клиент подключен");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        clients.forEach(p -> p.sendMessage(message));

    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nick) {
        return clients.stream()
                .anyMatch(p -> nick.equals(p.getNick()));

    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void sendPrivateMessage(String privateNick, String privateMessage, String fromNick) {
        final String sendMsg = "/w" + fromNick + "/w" + privateMessage;
        clients.stream()
                .filter(p -> privateNick.equals(p.getNick()))
                .forEach(p -> p.sendMessage(sendMsg));
    }
}
