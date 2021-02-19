package bg.sofia.uni.fmi.mjt.dungeonsonline.server.network;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.PlayerCommand;

public enum MessageType {
    DIALOG_BEGIN("DIALOG_BEGIN"),
    DIALOG_END("DIALOG_END"),
    MAP("MAP"),
    MESSAGE("MESSAGE");

    private final String actual;

    MessageType(String actual) {
        this.actual = actual;
    }

    public String toString() {
        return this.actual;
    }

    public static MessageType fromString(String commandAsString) {
        for (MessageType type : MessageType.values()) {
            if (type.actual.equalsIgnoreCase(commandAsString)) {
                return type;
            }
        }
        return null;
    }
}
