package ru.gb.mikenord.gb_java_chat.server;

import java.io.Closeable;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);

}
