package dungeonsonline.server.network.exceptions;

public class ServerRunningInterruptedException extends RuntimeException {
    public ServerRunningInterruptedException(String msg, Exception e) {
        super(msg, e);
    }
}
