package dungeonsonline.dungeonsclient.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class MessageSender implements Runnable{

    private static final String STOPPED_SERVER_MESSAGE = System.lineSeparator() + "Server has stopped working!";

    private static final int BUFFER_SIZE = 2048;
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private final SocketChannel socketChannel;

    public MessageSender(SocketChannel serverSocketChannel) {
        this.socketChannel = serverSocketChannel;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            try {
                writeToServer(line, socketChannel);
            } catch (IOException e) {
                System.out.println(STOPPED_SERVER_MESSAGE);
                System.exit(1);
            }
        }
    }

    private void writeToServer(String msg, SocketChannel serverSocketChannel) throws IOException {
        buffer.clear();
        buffer.put(msg.getBytes());
        buffer.flip();
        serverSocketChannel.write(buffer);
    }
}
