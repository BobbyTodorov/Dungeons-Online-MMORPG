package dungeonsonline.dungeonsclient.network;

import dungeonsonline.dungeonsclient.ui.UserInterface;
import dungeonsonline.dungeonsclient.ui.UserInterfaceImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;


public class MessageReceiver implements Runnable {

    private static final String STOPPED_SERVER_MESSAGE = System.lineSeparator() + "Server has stopped working!";
    private static final String DISCONNECT_MESSAGE = System.lineSeparator() + "You are being disconnected.";

    private static final String DEAD_MESSAGE = "You died!";

    private final SocketChannel socketChannel;
    private static final int BUFFER_SIZE = 2048;
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private static final UserInterface ui = UserInterfaceImpl.getInstance();

    public MessageReceiver(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String messageFromServer = receiveFromServer(socketChannel);

                if (messageFromServer == null) {
                    continue;
                }

                if (messageFromServer.equals(DISCONNECT_MESSAGE) || messageFromServer.equals(DEAD_MESSAGE)) {
                    System.out.println(messageFromServer);
                    System.exit(0);
                }

                System.out.println(messageFromServer);

                if (messageFromServer.contains("Hero ")) {
                    System.out.println(ui.commands());
                }
            }
        } catch (IOException e) {
            System.out.println(STOPPED_SERVER_MESSAGE);
            System.exit(0);
        }
    }

    private String receiveFromServer(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        if (clientChannel.read(buffer) < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();
        byte[] serverInputBytes = new byte[buffer.remaining()];
        buffer.get(serverInputBytes);

        return new String(serverInputBytes, StandardCharsets.UTF_8);
    }
}
