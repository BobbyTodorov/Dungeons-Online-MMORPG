package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Backpack;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.gameengine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.ClientRequest;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.NetworkServer;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.PlayersConnectionStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DungeonsOnlineServer {

    private static final int MAX_NUMBER_OF_PLAYERS_CONNECTED = 9;

    private static final String WRONG_COMMAND_MESSAGE = "Wrong command.";
    private static final String ENTER_YOUR_NAME_MESSAGE = "Enter your name: ";
    private static final String GAME_ON_MESSAGE = "Game On!";
    private static final String DISCONNECT_MESSAGE = "You are being disconnected.";
    private static final String TREASURE_INTERACTION_MESSAGE = "%s found. c (consume) / b (to backpack).";
    private static final String PLAYER_INTERACTION_MESSAGE = "interacting with %s, a (attack) / t (trade from backpack).";
    private static final String CHOOSE_INDEX_FROM_BACKPACK_MESSAGE = "Please choose treasure by index or c (cancel).";
    private static final String BACKPACK_TREASURE_FUNCTION_MESSAGE = "Please choose: u (use) / d (drop)";

    private static final String TREASURE = "treasure";
    private static final String PLAYER = "player";
    private static final String STATUS_DEAD = "dead";

    private static final NetworkServer networkServer = NetworkServer.getInstance();
    private static final PlayersConnectionStorage playersConnectionStorage = PlayersConnectionStorage.getInstance(MAX_NUMBER_OF_PLAYERS_CONNECTED);
    private static final GameEngine gameEngine = GameEngine.getInstance(MAX_NUMBER_OF_PLAYERS_CONNECTED);
    private static final StaticObjectsStorage staticObjectStorage = StaticObjectsStorage.getInstance();

    public static void main(String[] args) throws IOException {
        new DungeonsOnlineServer().start();
    }

    public void start() throws IOException {
        networkServer.start();
        while(true) {
            ClientRequest newRequest = networkServer.pollClientRequest();
            analyzeClientRequest(newRequest);
        }
    }

    private void analyzeClientRequest(ClientRequest clientRequest) throws IOException {
        if (clientRequest != null) {
            PlayerCommands command = PlayerCommands.fromString(clientRequest.clientCommand());
            if (command == null) {
                return;
            }

            SocketChannel client = clientRequest.clientSocketChannel();

            String commandResult = executeCommandForClient(command, client);

            if (commandResult != null) {
                if (commandResult.equals(STATUS_DEAD)) {
                    endPlayerSession(client);
                    return;
                }
                networkServer.writeToClient(commandResult, client);
            }

            synchronizeClientsMap();
            networkServer.writeToClient(playersConnectionStorage.getPlayerHero(client).toString(), client); //TODO proper UI
        }
    }

    private String executeCommandForClient(PlayerCommands command, SocketChannel client) throws IOException {
        switch (command) {
            case START -> { return startPlayerSession(client); }
            case DISCONNECT -> { endPlayerSession(client); }
            case BACKPACK -> { return openBackpack(client); }

            case UP -> { return executeMove(client, Direction.UP); }
            case DOWN -> { return executeMove(client, Direction.DOWN); }
            case LEFT -> { return executeMove(client, Direction.LEFT); }
            case RIGHT -> { return executeMove(client, Direction.RIGHT); }

            default -> { return WRONG_COMMAND_MESSAGE; }
        }

        return null;
    }

    private String startPlayerSession(SocketChannel clientChannel) throws IOException {
        networkServer.writeToClient(ENTER_YOUR_NAME_MESSAGE, clientChannel);
        String heroName;
        do {
            heroName = networkServer.readFromClient(clientChannel);
        } while (heroName == null);
        Hero newHero = new Hero(heroName);
        playersConnectionStorage.connectPlayer(clientChannel, newHero);
        gameEngine.summonPlayerHero(newHero);
        return GAME_ON_MESSAGE;
    }

    private void endPlayerSession(SocketChannel clientChannel) throws IOException {
        gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHero(clientChannel));
        playersConnectionStorage.disconnectPlayer(clientChannel);
        clientChannel.close();
    }

    private String openBackpack(SocketChannel client) throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHero(client);
        Backpack playerBackpack = playerHero.getBackpack();

        int treasureIndex = getTreasureFromBackpackIndexFromPlayer(client, playerHero);
        if (treasureIndex == -1) {
            return null;
        }
        Treasure treasure = playerBackpack.getTreasureAt(treasureIndex);

        networkServer.writeToClient(BACKPACK_TREASURE_FUNCTION_MESSAGE, client);

        String commandStr;
        do {
            commandStr = networkServer.readFromClient(client);
        } while (commandStr == null || (!commandStr.equals(PlayerCommands.DROP.getActual()) && !commandStr.equals(PlayerCommands.USE.getActual())));

        PlayerCommands command = PlayerCommands.fromString(commandStr);
        return executeCommandOnHeroTreasure(command, playerHero, treasure);
    }

    private String executeCommandOnHeroTreasure(PlayerCommands command, Hero hero, Treasure treasure) {
        switch (command) {
            case DROP -> {
                //TODO appear next to hero
            }
            case USE -> { return treasure.collect(hero); }
        }

        return null;
    }

    private int getTreasureFromBackpackIndexFromPlayer(SocketChannel client, Hero clientHero) throws IOException {
        networkServer.writeToClient(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE, client);
        networkServer.writeToClient(clientHero.getBackpack().toString(), client);
        String clientAnswer;
        int treasureIndex = 0;
        do {
            clientAnswer = networkServer.readFromClient(client);
            if (clientAnswer == null) {
                continue;
            }
            if (clientAnswer.equals(PlayerCommands.CANCEL.getActual())) {
                return -1;
            }
            treasureIndex = Integer.parseInt(clientAnswer.trim());
        } while (treasureIndex <= 0 || treasureIndex > clientHero.getBackpack().size());

        return treasureIndex - 1;
    }

    private void synchronizeClientsMap() throws IOException {
        String map = gameEngine.getMapToVisualize();
        networkServer.writeToAllClients(map, playersConnectionStorage.getPlayersSocketChannels());
    }

    private String executeMove(SocketChannel client, Direction direction) throws IOException {
        Hero clientHero = null;
        if (playersConnectionStorage.isPlayerAlreadyConnected(client)) {
            clientHero = playersConnectionStorage.getPlayerHero(client);
        }

        String moveHeroResult = gameEngine.moveHero(clientHero, direction);

        if (moveHeroResult.equals(TREASURE)) {
            return interactWithTreasure(client);
        }

        if (moveHeroResult.contains(PLAYER)) {
            Hero otherHero = playersConnectionStorage.getPlayerHeroByHeroSymbol((moveHeroResult.split(" ")[1].charAt(0)));
            return interactWithPlayer(client, otherHero);
        }

        if (!clientHero.isAlive()) {//TODO dead+disconnect
            networkServer.writeToClient(DISCONNECT_MESSAGE, client);
            endPlayerSession(client);
            return STATUS_DEAD;
        }

        return moveHeroResult;
    }

    private String interactWithTreasure(SocketChannel client) throws IOException {
        Treasure treasure = staticObjectStorage.getTreasure();
        networkServer.writeToClient(String.format(TREASURE_INTERACTION_MESSAGE, treasure.toString()), client);
        String answer;
        do {
            answer = networkServer.readFromClient(client);
        } while (answer == null);

        if (answer.equals(PlayerCommands.CONSUME.getActual())) {
            return gameEngine.consumeTreasure(client, treasure);
        } else {
            return gameEngine.addTreasureToBackpack(client, treasure);
        }
    }

    private String interactWithPlayer(SocketChannel client, Hero otherHero) throws IOException {
        Hero clientHero = playersConnectionStorage.getPlayerHero(client);
        networkServer.writeToClient(String.format(PLAYER_INTERACTION_MESSAGE, otherHero.getName()), client);
        String answer;
        do {
            answer = networkServer.readFromClient(client);
        } while (answer == null);

        if (answer.equals(PlayerCommands.ATTACK.getActual())) {
            return gameEngine.battleWithPlayer(clientHero, otherHero);
        } else {
            int treasureIndex = getTreasureFromBackpackIndexFromPlayer(client, clientHero);
            if (treasureIndex == -1) {
                return null;
            }
            return gameEngine.tradeWithPlayer(clientHero, otherHero, treasureIndex);
        }
    }




}
