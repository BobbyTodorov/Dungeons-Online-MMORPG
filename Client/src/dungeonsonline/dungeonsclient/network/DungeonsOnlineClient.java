package dungeonsonline.dungeonsclient.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class DungeonsOnlineClient {

    private static final String INITIAL_MESSAGE = "Connected to the server. Type 'start' to begin playing.";
    private static final String SERVER_CONNECTION_PROBLEM_MESSAGE = "There is a problem with the connection.";

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;

    public static void main(String[] args) {
        new DungeonsOnlineClient().start();
    }

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open()) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println(INITIAL_MESSAGE);

            Thread receiver = new Thread(new MessageReceiver(socketChannel));

            Thread sender = new Thread(new MessageSender(socketChannel));
            receiver.start();
            sender.start();
            receiver.join();
            sender.join();

        } catch (IOException | InterruptedException e) {
            System.err.println(SERVER_CONNECTION_PROBLEM_MESSAGE);
        }
    }
}