package ru.gb.mikenord.gb_java_chat.server;

import ru.gb.mikenord.gb_java_chat.ChatCommand;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;
import java.util.stream.Collectors;


import static ru.gb.mikenord.gb_java_chat.ChatCommand.*;

public class ChatServer {
    private final Map<String, ClientHandler> clients;
    public final Logger serverLogger;
    private final DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public ChatServer() {
        this.clients = new HashMap<>();
        this.serverLogger = Logger.getLogger(ChatServer.class.getName());
        serverLogger.setUseParentHandlers(false);
        Handler serverLogHandler = new ConsoleHandler();
        serverLogHandler.setLevel(Level.INFO);
        serverLogHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "[" + format.format(Date.from(record.getInstant())) + "]  -->  "
                        + record.getMessage() + "\n";
            }
        });
        this.serverLogger.addHandler(serverLogHandler);

    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new DatabaseAuthService()) {
            while (true) {
//                System.out.println("Ожидаю подключения...");
                serverLogger.info("Ожидание нового подключения...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                serverLogger.info("Новый клиент подключается...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(ChatCommand command, String message) {
        clients.values().forEach(p -> p.sendMessage(command, message));
        serverLogger.info("-- команда: '"+ command.collectMessage() + "' -> сообщение: " + message); // TODO
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
        serverLogger.info("Пользователь '" + client.getNick() + "' покинул чат");
        broadcastClientsList();
    }

    public void sendPrivateMessage(ClientHandler from, String toNick, String privateMessage) {
        clients.get(toNick).sendMessage(MESSAGE, "От '" + from.getNick() + "': " + privateMessage);
        from.sendMessage(MESSAGE, "Для " + toNick + ": " + privateMessage);
        serverLogger.info("Приватное сообщение от '" + from.getNick() + "' для '" + toNick + "'");

    }

    public void changeNick(String[] params) {
        clients.put(params[1], clients.get(params[0]));
        clients.remove(params[0]);
        broadcastClientsList();
        broadcast(MESSAGE, "Пользователь '" + params[0] + "' сменил ник на '" + params[1] + "'");
    }
}
