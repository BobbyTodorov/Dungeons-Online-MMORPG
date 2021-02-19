package bg.sofia.uni.fmi.mjt.dungeonsonline.server.network;

// These are used to tell the client what type of message the server is sending,
// so that the client can manage the messages and user interface correctly and easier.
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
