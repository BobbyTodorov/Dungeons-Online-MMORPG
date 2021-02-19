package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

public enum PlayerCommand {
    START("start"),
    DISCONNECT("dc"),
    UP("u"),
    DOWN("d"),
    LEFT("l"),
    RIGHT("r"),
    BACKPACK("bp"),
    CANCEL("cancel"),
    DROP("d"),
    USE("u"),
    ATTACK("a"),
    TRADE("t"),
    COLLECT("b"),
    CONSUME("c");

    private final String actual;

    PlayerCommand(String actual) {
        this.actual = actual;
    }

    public String toString() {
        return this.actual;
    }

    public static PlayerCommand fromString(String commandAsString) {
        for (PlayerCommand command : PlayerCommand.values()) {
            if (command.actual.equalsIgnoreCase(commandAsString)) {
                return command;
            }
        }
        return null;
    }
}
