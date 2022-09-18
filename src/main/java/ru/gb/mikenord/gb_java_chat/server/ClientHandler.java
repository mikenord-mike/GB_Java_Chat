package ru.gb.mikenord.gb_java_chat.server;

import ru.gb.mikenord.gb_java_chat.ChatCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.gb.mikenord.gb_java_chat.ChatCommand.*;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatServer server;
    private String nick;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    final boolean isAuth = authenticate();
                    if (isAuth) {
                        readMessages();
                    } else {
                        sendMessage(END);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticate() throws InterruptedException {
        while (true) {
            try {
                final String message = in.readUTF();
                final ChatCommand command = ChatCommand.getCommand(message);
                final String[] params = command.parse(message);
                if (command == END) {
                    return false;
                }
                if (command == AUTH_REQUEST) {
                    final String login = params[0];
                    final String password = params[1];
                    final String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage(ERROR, "такой пользователь уже авторизован");
                            server.serverLogger.warning("Ошибка авторизации - такой пользователь уже авторизован");
                            continue;
                        }
                        sendMessage(AUTH_SUCCESS, nick);
                        this.nick = nick;
                        server.broadcast(MESSAGE, "Пользователь " + nick + " зашел в чат");
                        server.serverLogger.warning("Успешная авторизация - "+ nick + " зашел в чат");
                        server.subscribe(this);
                        return true;
                    } else {
                        sendMessage(ERROR, "неверные логин и пароль");
                        server.serverLogger.warning("Ошибка авторизации - неверные логин и пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage(END);
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
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
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

    private void readMessages() {
        while (true) {
            try {
                final String message = in.readUTF();
                final ChatCommand command = ChatCommand.getCommand(message);
                final String[] params = command.parse(message);
                if (command == END) {
                    break;
                }
                if (command == PRIVATE_MESSAGE) {
                    final String toNick = params[0];
                    final String privateMessage = params[1];
                    if (server.isNickBusy(toNick)) {
                        server.sendPrivateMessage(this, toNick, privateMessage);
                    } else {
                        sendMessage(ERROR, "Пользователь '" + toNick + "' не авторизован");
                    }
                } else if (command == MESSAGE) {
                    server.broadcast(MESSAGE, nick + ": " + params[0]);
                } else if (command == NICK_CHANGE) {
                    this.nick = params[1];
                    authService.nickChange(params);
                    server.changeNick(params);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNick() {
        return nick;
    }
}