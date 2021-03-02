package dungeonsonline.server.storage.exceptions;

public class MaxNumberOfPlayersReachedException extends Exception {
    public MaxNumberOfPlayersReachedException(String msg) {
        super(msg);
    }
}
