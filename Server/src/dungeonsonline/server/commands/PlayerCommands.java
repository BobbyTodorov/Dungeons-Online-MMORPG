package dungeonsonline.server.commands;

public enum PlayerCommands {
    START("start"),
    DISCONNECT("dc"),
    UP("u"),
    DOWN("d"),
    LEFT("l"),
    RIGHT("r"),
    BACKPACK("bp"),
    CANCEL("cancel"),
    ATTACK("a"),
    TRADE("t"),
    USE("use"),
    COLLECT("c"),
    DROP("dr");

    private final String actual;

    PlayerCommands(String actual) {
        this.actual = actual;
    }

    public String toString() {
        return this.actual;
    }

    public static PlayerCommands fromString(String commandAsString) {
        for (PlayerCommands command : PlayerCommands.values()) {
            if (command.actual.equalsIgnoreCase(commandAsString)) {
                return command;
            }
        }
        return null;
    }
}
