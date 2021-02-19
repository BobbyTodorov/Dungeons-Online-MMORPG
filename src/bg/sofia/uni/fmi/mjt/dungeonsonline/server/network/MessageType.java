package bg.sofia.uni.fmi.mjt.dungeonsonline.server.network;

public enum MessageType {
    DIALOGING("DIALOGING"),
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
}
