package dungeonsonline.server.network;

import dungeonsonline.server.Logger;
import dungeonsonline.server.network.exceptions.ServerClosureFailException;
import dungeonsonline.server.network.exceptions.ServerCreationFailedException;
import dungeonsonline.server.network.exceptions.ServerRunningInterruptedException;
import dungeonsonline.server.validator.ArgumentValidator;

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
    private static final String SC_CONNECTION_ACCEPTED = "Connection accepted from client %s";
    private static final String SC_CONNECTION_INTERRUPTED = "%s connection interrupted.";

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static Selector selector;
    private static ServerSocketChannel serverSocketChannel;

    private static final int BUFFER_SIZE = 2048;
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);


    private static boolean isRunning;

    private final static Queue<ClientRequest> receivedClientRequests = new LinkedBlockingQueue<>();
    private final static Logger logger = Logger.getInstance();

    /**
     * @return next ClientRequest waiting to be processed
     */
    public ClientRequest pollNextClientRequest() {
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

    public synchronized String readFromSocketChannel(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        if (clientChannel.read(buffer) <= 0) {
            return null;
        }

        buffer.flip();
        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);
        String readString = new String(clientInputBytes, StandardCharsets.UTF_8);

        String logString =
            "Message [" + readString.trim() + "] received from clientChannel " + clientChannel.getRemoteAddress();
        logger.log(logString);

        return readString.trim();
    }

    public synchronized void writeToSocketChannel(String msg, SocketChannel clientSocketChannel)
        throws IOException {
        buffer.clear();
        buffer.put(msg.getBytes());
        buffer.flip();

        String logString = "Sending message to clientChannel: ";
        logger.log(logString);

        logString = msg;
        logger.log(logString);

        clientSocketChannel.write(buffer);
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void run() throws IOException {
        // Currently only one Thread is receiving client requests (it's enough for now).
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
                    clientInput = readFromSocketChannel(clientChannel);
                } catch (SocketException e) {
                    String logString = String.format(SC_CONNECTION_INTERRUPTED, clientChannel.getRemoteAddress());
                    logger.log(logString);
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

    private void addReceivedClientRequest(SocketChannel clientChannel, String request) {
        ArgumentValidator.checkForNullArguments(clientChannel, request);

        receivedClientRequests.add(new ClientRequest(clientChannel, request));
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientSocketChannel = serverSocketChannel.accept();

        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);

        String logString = String.format(SC_CONNECTION_ACCEPTED, clientSocketChannel.getRemoteAddress());
        logger.log(logString);
    }
}
