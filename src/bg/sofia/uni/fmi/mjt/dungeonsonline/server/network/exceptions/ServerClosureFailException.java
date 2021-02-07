package bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.exceptions;

public class ServerClosureFailException extends RuntimeException {
    public ServerClosureFailException(String msg, Exception e) {
        super(msg, e);
    }
}
