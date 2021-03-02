package dungeonsonline.server;

public class ProcessPlayerRequestFailedException extends RuntimeException {
    public ProcessPlayerRequestFailedException(String msg, Exception e) {
        super(msg, e);
    }
}
