module ru.gb.mikenord.gb_java_chat {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.gb.mikenord.gb_java_chat to javafx.fxml;
    exports ru.gb.mikenord.gb_java_chat;
}