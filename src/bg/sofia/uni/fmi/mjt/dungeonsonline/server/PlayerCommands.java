package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

public enum PlayerCommands {
    START("start"),
    DISCONNECT("disconnect"),
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right"),
    BACKPACK("backpack"),
    CANCEL("cancel"),
    DROP("d"),
    USE("u"),
    ATTACK("a"),
    TRADE("t"),
    COLLECT("b"),
    CONSUME("c");

    private final String actual;

    PlayerCommands(String actual) {
        this.actual = actual;
    }

    public String getActual() {
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
