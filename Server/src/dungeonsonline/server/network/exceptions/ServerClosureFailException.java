package dungeonsonline.server.network.exceptions;

public class ServerClosureFailException extends RuntimeException {
    public ServerClosureFailException(String msg, Exception e) {
        super(msg, e);
    }
}
