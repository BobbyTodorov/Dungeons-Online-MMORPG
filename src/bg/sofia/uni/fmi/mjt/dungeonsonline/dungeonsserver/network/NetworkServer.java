package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.network;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.gameengine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.storage.PlayersConnectionStorage;

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
import java.util.List;
import java.util.Set;

public class NetworkServer {

    private static final int MAX_NUMBER_OF_PLAYERS_CONNECTED = 9;

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private static final int BUFFER_SIZE = 2048;
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private boolean isRunning;
    private static final GameEngine gameEngine = new GameEngine();
    private static final PlayersConnectionStorage playersConnectionStorage = PlayersConnectionStorage.getInstance(MAX_NUMBER_OF_PLAYERS_CONNECTED);



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
                    throw new RuntimeException("Server running was interrupted", e);
                    //throw new RunningInterruptedException("Server running was interrupted", e);
                }
            }).start();
        } catch (IOException e) {
            System.out.println("server creation failed");
            //throw new CreationFailedException("Server creation failed", e);
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
                System.out.println("closing server failed");
                //throw new ClosureFailed("Closing server failed", e);
            }
        }
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

            operateSocketChannels(keyIterator);
        }
    }

    private void operateSocketChannels(Iterator<SelectionKey> keyIterator) throws IOException {
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) key.channel();

                String clientInput = null;
                try {
                    clientInput = readFromClient(clientChannel);
                } catch (SocketException e) {
                    System.out.println(clientChannel.getRemoteAddress() + " disconnected");
                    endClientSession(clientChannel);
                }

                if (clientInput == null) {
                    continue;
                }
                if (clientInput.equals("dc")) {
                    endClientSession(clientChannel);
                    continue;
                }

                if (!playersConnectionStorage.isPlayerAlreadyConnected(clientChannel)) {
                    startClientSession(clientChannel);
                }

                switch (clientInput) {
                    case "up" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.UP); }
                    case "down" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.DOWN); }
                    case "left" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.LEFT); }
                    case "right" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.RIGHT); }
                }

                String map = gameEngine.getMapToVisualize();
                writeToAllClients(map, playersConnectionStorage.getPlayersSocketChannels());

            } else if (key.isAcceptable()) {
                accept(selector, key);
            }
            keyIterator.remove();
        }
    }


    private void startClientSession(SocketChannel clientChannel) throws IOException {
        writeToClient("Enter your name:", clientChannel);
        String heroName;
        do {
            heroName = readFromClient(clientChannel);
        } while (heroName == null);
        Hero newHero = new Hero(heroName);
        playersConnectionStorage.connectPlayer(clientChannel, newHero);
        gameEngine.summonPlayerHero(newHero);
    }

    private void endClientSession(SocketChannel clientChannel) throws IOException {
        gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHero(clientChannel));
        playersConnectionStorage.disconnectPlayer(clientChannel);
        clientChannel.close();
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);

        System.out.println("Connection accepted from client " + accept.getRemoteAddress());
    }

    private String readFromClient(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        if (clientChannel.read(buffer) == 0) {
            return null;
        }
        if (clientChannel.read(buffer) == -1) {
            return "disconnected";
        }

        buffer.flip();
        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);
        String readString = new String(clientInputBytes, StandardCharsets.UTF_8);
        System.out.println("Message [" + readString.trim() + "] received from client " + clientChannel.getRemoteAddress());

        return readString.trim();
    }

    private void writeToClient(String msg, SocketChannel clientSocketChannel) throws IOException {
        buffer.clear();
        buffer.put(msg.getBytes());
        buffer.flip();
        System.out.println("Sending message to client: ");
        System.out.println(msg);
        clientSocketChannel.write(buffer);
    }

    private void writeToAllClients(String msg, Set<SocketChannel> clientSocketChannels) throws IOException {
        for (SocketChannel clientSocketChannel : clientSocketChannels) {
            writeToClient(msg, clientSocketChannel);
        }
    }
}
