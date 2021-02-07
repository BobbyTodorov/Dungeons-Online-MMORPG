package bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.exceptions;

public class OutOfMapBoundsException extends ArrayIndexOutOfBoundsException {
    public OutOfMapBoundsException(String msg) {
        super(msg);
    }
}
