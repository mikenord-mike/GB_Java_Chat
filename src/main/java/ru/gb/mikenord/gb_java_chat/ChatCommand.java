package ru.gb.mikenord.gb_java_chat;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ChatCommand {
    AUTH_REQUEST("/auth") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1], split[2]};
        }
    },

    AUTH_SUCCESS("/auth_ok") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1]};
        }
    },

    NICK_CHANGE("/nick_change") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1], split[2]};
        }
    },

    END("/end") {
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },

    PRIVATE_MESSAGE("/w") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 3);
            return new String[]{split[1], split[2]};
        }
    },

    CLIENT_LIST("/clients") {
        @Override
        public String[] parse(String commandText) {
            return Arrays.stream(commandText.split(TOKEN_DELIMITER))
                    .skip(1)
                    .toArray(String[]::new);
        }
    },

    ERROR("/error") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    },

    MESSAGE("/msg") {
        @Override
        public String[] parse(String commandText) {
            final String[] split = commandText.split(TOKEN_DELIMITER, 2);
            return new String[]{split[1]};
        }
    };

    private final String command;
    static final String TOKEN_DELIMITER = "\\p{Blank}+";
    static final Map<String, ChatCommand> commandMap = Arrays.stream(values())
            .collect(Collectors.toMap(ChatCommand::getCommand, Function.identity()));

     ChatCommand(String command) {
        this.command = command;
    }

    private String getCommand() {
        return command;
    }

    public static ChatCommand getCommand(String message) {
        if (!message.startsWith("/")) {
            throw new RuntimeException("'" + message + "' is not a command");
        }
        String cmd = message.split(TOKEN_DELIMITER, 2)[0];
        ChatCommand command = commandMap.get(cmd);
        if (command == null) {
            throw new RuntimeException("Unknown command '" + cmd + "'");
        }
        return command;
    }

    public abstract String[] parse(String commandText);

    public String collectMessage(String... params) {
        return this.command + " " + String.join(" ", params);
    }

}
