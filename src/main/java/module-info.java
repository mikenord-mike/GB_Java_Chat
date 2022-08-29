module ru.gb.mikenord.gb_java_chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    exports ru.gb.mikenord.gb_java_chat.client;
    opens ru.gb.mikenord.gb_java_chat.client to javafx.fxml;
}