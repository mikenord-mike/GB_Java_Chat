package ru.gb.mikenord.gb_java_chat.server;

import ru.gb.mikenord.gb_java_chat.ChatCommand;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.gb.mikenord.gb_java_chat.ChatCommand.*;

public class ChatServer {
    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new DatabaseAuthService()) {
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

    public void broadcast(ChatCommand command, String message) {
        clients.values().forEach(p -> p.sendMessage(command, message));
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientsList();
    }

    private void broadcastClientsList() {
        String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));
        broadcast(CLIENT_LIST, nicks);
    }

    public boolean isNickBusy(String nick) {
        return clients.get(nick) != null;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientsList();
    }

    public void sendPrivateMessage(ClientHandler from, String toNick, String privateMessage) {
        clients.get(toNick).sendMessage(MESSAGE, "От '" + from.getNick() + "': " + privateMessage);
        from.sendMessage(MESSAGE, "Для " + toNick + ": " + privateMessage);
    }
}
