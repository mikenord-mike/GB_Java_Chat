package ru.gb.mikenord.gb_java_chat.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAuthService implements AuthService {

    private static class UserData {
        private String nick;
        private final String login;
        private final String passwordDigest;

        public UserData(String nick, String login, String passwordDigest) {
            this.nick = nick;
            this.login = login;
            this.passwordDigest = passwordDigest;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPasswordDigest() {
            return passwordDigest;
        }
    }

    private final List<UserData> users;

    public DatabaseAuthService() {
        users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("" +
                "jdbc:sqlite:/media/mikenord/my_Share/__GB/GB_Java_Chat/src/main/" +
                "resources/ru/gb/mikenord/gb_java_chat/client/clients.db")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT nickname,login,password FROM auth;");
            while (resultSet.next()) {
                users.add(new UserData(resultSet.getString(1),
                        resultSet.getString(2),
                        resultSet.getString(3)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDigestFromPassword(String password) {
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-512");
            byte[] hashbytes = digester.digest(password.getBytes());

            StringBuffer result = new StringBuffer();
            for (byte b : hashbytes) {
                result.append(String.format("%02X", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        return users.stream()
                .filter(user -> login.equals(user.getLogin())
                        && getDigestFromPassword(password).equals(user.getPasswordDigest()))
                .map(UserData::getNick)
                .findAny()
                .orElse(null);
    }

    @Override
    public boolean nickChange(String[] params) {
        if (params == null || params.length != 2) {
            return false;
        }

        String sqlString = String.format("UPDATE auth SET nickname = '%s' WHERE nickname = '%s';", params[1], params[0]);

        try (Connection connection = DriverManager.getConnection("" +
                "jdbc:sqlite:/media/mikenord/my_Share/__GB/GB_Java_Chat/src/main/" +
                "resources/ru/gb/mikenord/gb_java_chat/client/clients.db")) {
            Statement statement = connection.createStatement();
            int resultUpdate = statement.executeUpdate(sqlString);
            if (resultUpdate > 0) {
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).nick.equals(params[0])) {
                        users.get(i).nick = params[1];
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
