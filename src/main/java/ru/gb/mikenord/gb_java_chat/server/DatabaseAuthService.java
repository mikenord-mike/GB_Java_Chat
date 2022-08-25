package ru.gb.mikenord.gb_java_chat.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAuthService implements AuthService {

    private static class UserData {
        private final String nick;
        private final String login;
        private final String password;

        public UserData(String nick, String login, String password) {
            this.nick = nick;
            this.login = login;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    private final List<UserData> users;

    public DatabaseAuthService() {
        users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("" +
                "jdbc:sqlite:/media/mikenord/my_Share/__GB/GB_Java_Chat/src/main/" +
                "resources/ru/gb/mikenord/gb_java_chat/client/clients.db")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet =  statement.executeQuery("SELECT nickname,login,password FROM auth;");
            while (resultSet.next()) {
                users.add(new UserData(resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        return users.stream()
                .filter(user -> login.equals(user.getLogin())
                        && password.equals(user.getPassword()))
                .map(UserData::getNick)
                .findAny()
                .orElse(null);
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
