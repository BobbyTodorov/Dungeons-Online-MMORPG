package bg.sofia.uni.fmi.mjt.dungeonsonline.server.network;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.exceptions.ServerClosureFailException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.exceptions.ServerCreationFailedException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.exceptions.ServerRunningInterruptedException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkServer {

    private static final String SERVER_RUNNING_INTERRUPTED_EXCEPTION_MESSAGE = "Server running was interrupted.";
    private static final String SERVER_CREATION_FAILED_EXCEPTION_MESSAGE = "Server creation failed.";
    private static final String SERVER_CLOSURE_FAIL_EXCEPTION_MESSAGE = "Server closing failed.";

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private static final int BUFFER_SIZE = 2048;
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private boolean isRunning;

    private final Queue<ClientRequest> receivedClientRequests = new LinkedBlockingQueue<>();


    private NetworkServer() {}

    private static NetworkServer instance = null;

    public static NetworkServer getInstance() {
        if (instance == null) {
            instance = new NetworkServer();
        }

        return instance;
    }

    public void addReceivedClientRequest(SocketChannel clientChannel, String request) {
        ArgumentValidator.checkForNullArguments(clientChannel, request);

        receivedClientRequests.add(new ClientRequest(clientChannel, request));
    }

    public ClientRequest pollClientRequest() {
        return receivedClientRequests.poll();
    }

    public void start() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            isRunning = true;

            new Thread(() -> {
                try {
                    run();
                } catch (IOException e) {
                    throw new ServerRunningInterruptedException(SERVER_RUNNING_INTERRUPTED_EXCEPTION_MESSAGE, e);
                }
            }).start();
        } catch (IOException e) {
            throw new ServerCreationFailedException(SERVER_CREATION_FAILED_EXCEPTION_MESSAGE, e);
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            if (selector.isOpen()) {
                selector.wakeup();
            }
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                throw new ServerClosureFailException(SERVER_CLOSURE_FAIL_EXCEPTION_MESSAGE, e);
            }
        }
    }

    public String readFromClient(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        if (clientChannel.read(buffer) <= 0) {
            return null;
        }

        buffer.flip();
        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);
        String readString = new String(clientInputBytes, StandardCharsets.UTF_8);
        System.out.println("Message [" + readString.trim() + "] received from client " + clientChannel.getRemoteAddress());

        return readString.trim();
    }

    public void writeToClient(String msg, SocketChannel clientSocketChannel) throws IOException {
        buffer.clear();
        buffer.put(msg.getBytes());
        buffer.flip();
        System.out.println("Sending message to client: ");
        System.out.println(msg);
        clientSocketChannel.write(buffer);
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void run() throws IOException {
        while (isRunning) {
            if (selector.select() == 0) {
                continue;
            }
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            receiveSocketChannelsClientRequests(keyIterator);
        }
    }

    private void receiveSocketChannelsClientRequests(Iterator<SelectionKey> keyIterator) throws IOException {
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) key.channel();

                String clientInput;
                try {
                    clientInput = readFromClient(clientChannel);
                } catch (SocketException e) {
                    System.out.println(clientChannel.getRemoteAddress() + " connection interrupted in NetworkServer");
                    clientChannel.close();
                    continue;
                }
                if (clientInput != null) {
                    addReceivedClientRequest(clientChannel, clientInput);
                }
            } else if (key.isAcceptable()) {
                accept(key);
            }
            keyIterator.remove();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientSocketChannel = serverSocketChannel.accept();

        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);

        System.out.println("Connection accepted from client " + clientSocketChannel.getRemoteAddress());
    }
}
