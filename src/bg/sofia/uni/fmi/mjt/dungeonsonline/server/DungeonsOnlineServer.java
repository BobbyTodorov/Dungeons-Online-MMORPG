package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.gameengine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.ClientRequest;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.NetworkServer;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.PlayersConnectionStorage;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DungeonsOnlineServer {

    private static final int MAX_NUMBER_OF_PLAYERS_CONNECTED = 9;

    private static final NetworkServer networkServer = NetworkServer.getInstance();
    private static final PlayersConnectionStorage playersConnectionStorage = PlayersConnectionStorage.getInstance(MAX_NUMBER_OF_PLAYERS_CONNECTED);
    private static final GameEngine gameEngine = GameEngine.getInstance();

    private boolean isRunning = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        new DungeonsOnlineServer().start();
    }

    public void start() throws IOException, InterruptedException {
        isRunning = true;
        networkServer.start();
        while(true) {
            analyzeClientsRequests();
        }
    }

    private void analyzeClientsRequests() throws IOException, InterruptedException {

        ClientRequest newRequest = networkServer.pollClientRequest();
        if (newRequest != null) {
            System.out.println(newRequest);
        }
//        if (newRequest != null) {
//            if (newRequest.clientMessage().equals("start")) {
//                startPlayerSession(newRequest.clientSocketChannel());
//            }
//        }

    }

    public void startPlayerSession(SocketChannel clientChannel) throws IOException {
        String heroName;
        do {
            networkServer.writeToClient("Please enter your name:", clientChannel);
            heroName = networkServer.readFromClient(clientChannel);
        } while (heroName == null);
        Hero newHero = new Hero(heroName);
        playersConnectionStorage.connectPlayer(clientChannel, newHero);
        gameEngine.summonPlayerHero(newHero);

    }

    public void endPlayerSession(SocketChannel clientChannel) throws IOException {
        gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHero(clientChannel));
        playersConnectionStorage.disconnectPlayer(clientChannel);
        clientChannel.close();
    }

    //TODO
    //                if (clientInput == null) {
//                    continue;
//                }
//                if (clientInput.equals("dc")) {
//                    endClientSession(clientChannel);
//                    continue;
//                }
//
//                if (!playersConnectionStorage.isPlayerAlreadyConnected(clientChannel)) {
//                    startClientSession(clientChannel);
//                }
//
//                switch (clientInput) {
//                    case "up" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.UP); }
//                    case "down" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.DOWN); }
//                    case "left" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.LEFT); }
//                    case "right" -> { gameEngine.moveHero(playersConnectionStorage.getPlayerHero(clientChannel), Direction.RIGHT); }
//                }
//
//                String map = gameEngine.getMapToVisualize();
//                writeToAllClients(map, playersConnectionStorage.getPlayersSocketChannels());

}
