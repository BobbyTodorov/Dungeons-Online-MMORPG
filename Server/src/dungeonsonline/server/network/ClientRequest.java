package dungeonsonline.server.network;

import java.nio.channels.SocketChannel;

public final record ClientRequest(SocketChannel clientChannel, String message) {

}
