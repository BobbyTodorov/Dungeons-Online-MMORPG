package dungeonsonline.server.network.exceptions;

public class ServerCreationFailedException extends RuntimeException {
    public ServerCreationFailedException(String msg, Exception e) {
        super(msg, e);
    }
}
