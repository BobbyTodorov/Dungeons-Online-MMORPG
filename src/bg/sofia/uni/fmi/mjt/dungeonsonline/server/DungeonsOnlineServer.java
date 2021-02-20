package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Backpack;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.gameengine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.ClientRequest;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.MessageType;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.NetworkServer;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.playercommands.PlayerCommand;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.PlayersConnectionStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.exceptions.MaxNumberOfPlayersReachedException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DungeonsOnlineServer {
    private static final int MAX_NUMBER_OF_PLAYERS = 9;

    private static final int PLAYER_STARTING_HEALTH = 100;
    private static final int PLAYER_STARTING_MANA = 100;
    private static final int PLAYER_STARTING_ATTACK = 50;
    private static final int PLAYER_STARTING_DEFENSE = 50;

    private static final String ENTER_YOUR_NAME_MESSAGE = "Enter your name: ";
    private static final String GAME_ON_MESSAGE = "Game On!";
    private static final String DISCONNECT_MESSAGE = System.lineSeparator() + "You are being disconnected.";
    private static final String WRONG_COMMAND_MESSAGE = "Wrong command.";
    private static final String UNKNOWN_COMMAND = "Unknown command.";

    private static final String TREASURE_INTERACTION_MESSAGE = "Treasure %s found. c (consume) / b (to backpack).";
    private static final String PLAYER_INTERACTION_MESSAGE =
        "Interacting with player %s, a (attack) / t (trade from backpack).";

    private static final String CHOOSE_INDEX_FROM_BACKPACK_MESSAGE =
        "Please choose treasure by index or 'cancel'." + System.lineSeparator();
    private static final String BACKPACK_TREASURE_COMMANDS_MESSAGE =
        "%s" + System.lineSeparator() + "Please choose: u (use) / d (drop)";

    private static final String DEAD_PLAYER_MESSAGE = "You died!";

    private static final String MAX_NUMBER_OF_PLAYERS_REACHED = "Max number of players reached. Please try later.";

    private static final NetworkServer networkServer = NetworkServer.getInstance();
    private static final PlayersConnectionStorage playersConnectionStorage =
        PlayersConnectionStorage.getInstance(MAX_NUMBER_OF_PLAYERS);

    private static final GameEngine gameEngine = GameEngine.getInstance();
    private static final StaticObjectsStorage staticObjectStorage = StaticObjectsStorage.getInstance();

    private boolean isRunning;

    public static void main(String[] args) {
        DungeonsOnlineServer test = new DungeonsOnlineServer();
        test.start();
    }

    public void start() {
        isRunning = true;
        networkServer.start();

        new Thread(this::run).start();
    }

    public void stop() {
        isRunning = false;
        networkServer.stop();
    }

    private void run() {
        ThreadPoolExecutor processor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_NUMBER_OF_PLAYERS);

        while (isRunning) {
            ClientRequest playerRequest = networkServer.pollNextClientRequest();

            if (playerRequest == null) {
                continue;
            }

            processor.execute(() -> {
                try {
                    processPlayerRequest(playerRequest);
                } catch (IOException ioException) {
                    throw new RuntimeException("Processing player request failed.");
                }
            });
        }
    }

    private void processPlayerRequest(ClientRequest playerRequest) throws IOException {
        PlayerCommand command = PlayerCommand.fromString(playerRequest.message());

        SocketChannel playerClient = playerRequest.clientChannel();

        try {
            String resultOfCommandExecution = executeCommandForPlayer(command, playerClient);

            if (resultOfCommandExecution != null) {
                sendMessageToSocketChannel(resultOfCommandExecution, MessageType.MAP, playerClient);
            }

            removeDisconnectedPlayersFromGame();
            displayNewGameFrameToPlayers();
        } catch (IOException e) {
            if (playersConnectionStorage.isPlayerClientConnected(playerClient)) {
                endPlayerClientSession(playerClient);
            }
        }
    }

    private void removeDisconnectedPlayersFromGame() {
        for (SocketChannel playerClient : playersConnectionStorage.getPlayersSocketChannels()) {
            if (!playerClient.isOpen()) {
                gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient));
            }
        }

        playersConnectionStorage.removePlayersWithInterruptedConnection();
    }

    private String executeCommandForPlayer(PlayerCommand command, SocketChannel client) throws IOException {

        if (command == null) {
            return WRONG_COMMAND_MESSAGE;
        }

        switch (command) {
            case START -> { return startPlayerSession(client); }
            case DISCONNECT -> { endPlayerClientSession(client); }
            case BACKPACK -> { return executeCommandOnPlayerBackpack(client); }

            case UP -> { return executePlayerMoveInGivenDirection(client, Direction.UP); }
            case DOWN -> { return executePlayerMoveInGivenDirection(client, Direction.DOWN); }
            case LEFT -> { return executePlayerMoveInGivenDirection(client, Direction.LEFT); }
            case RIGHT -> { return executePlayerMoveInGivenDirection(client, Direction.RIGHT); }

            default -> { return UNKNOWN_COMMAND; }
        }

        return null;
    }

    private String startPlayerSession(SocketChannel clientChannel) throws IOException {
        sendMessageToSocketChannel(ENTER_YOUR_NAME_MESSAGE, MessageType.MAP, clientChannel);

        String heroName = receiveMessageFromSocketChannel(clientChannel);

        Hero newHero = new Hero(heroName, getStartingPlayerStats());

        try {
            playersConnectionStorage.connectPlayer(clientChannel, newHero);
        } catch (MaxNumberOfPlayersReachedException e) {
            return MAX_NUMBER_OF_PLAYERS_REACHED;
        }

        gameEngine.summonPlayerHero(newHero);

        return GAME_ON_MESSAGE;
    }

    private Stats getStartingPlayerStats() {
        return new Stats(
            PLAYER_STARTING_HEALTH,
            PLAYER_STARTING_MANA,
            PLAYER_STARTING_ATTACK,
            PLAYER_STARTING_DEFENSE
        );
    }

    private void endPlayerClientSession(SocketChannel playerClient) throws IOException {
        sendMessageToSocketChannel(DISCONNECT_MESSAGE, MessageType.MESSAGE, playerClient);

        gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient));
        playersConnectionStorage.disconnectPlayerClient(playerClient);
        playerClient.close();
    }

    private String executeCommandOnPlayerBackpack(SocketChannel playerClient) throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);
        Backpack heroBackpack = playerHero.backpack();

        int treasureIndex = getTreasureInBackpackIndexFromPlayer(playerClient);
        if (treasureIndex == -1) { // cancelled
            sendMessageToSocketChannel("", MessageType.DIALOG_END, playerClient);
            return null;
        }

        Treasure treasure = heroBackpack.remove(treasureIndex);

        sendMessageToSocketChannel(String.format(BACKPACK_TREASURE_COMMANDS_MESSAGE, treasure),
            MessageType.DIALOGING, playerClient);

        String commandToTreasureString;
        do {
            commandToTreasureString = receiveMessageFromSocketChannel(playerClient);
        } while (!(commandToTreasureString.equals(PlayerCommand.DROP.toString())
            || commandToTreasureString.equals(PlayerCommand.USE.toString())));

        sendMessageToSocketChannel("", MessageType.DIALOG_END, playerClient);
        PlayerCommand commandToTreasure = PlayerCommand.fromString(commandToTreasureString);
        return gameEngine.executeCommandOnHeroTreasure(commandToTreasure, playerHero, treasure);
    }

    private String executePlayerMoveInGivenDirection(SocketChannel playerClient, Direction direction)
        throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

        String moveHeroResult = gameEngine.moveHero(playerHero, direction);

        if (moveHeroResult.equals(GameEngine.STEP_ON_TREASURE_STATUS)) {
            return interactWithTreasure(playerClient);
        }

        if (moveHeroResult.startsWith(GameEngine.STEP_ON_HERO_STATUS)) {
            // Here moveHeroResult = *stepped on hero status string* followed by that hero's symbol.
            char otherHeroSymbol = moveHeroResult.charAt(moveHeroResult.length() - 1);

            Hero otherHero = playersConnectionStorage.getPlayerHeroByHeroSymbol(otherHeroSymbol);
            return playerInteractWithAnotherHero(playerClient, otherHero);
        }

        return moveHeroResult;
    }

    private String interactWithTreasure(SocketChannel playerClient) throws IOException {
        Treasure treasure = staticObjectStorage.getTreasure();
        sendMessageToSocketChannel(String.format(TREASURE_INTERACTION_MESSAGE, treasure.toString()),
            MessageType.DIALOGING, playerClient);

        String commandToTreasure;
        do {
            commandToTreasure = receiveMessageFromSocketChannel(playerClient);
        } while (!(commandToTreasure.equals(PlayerCommand.CONSUME.toString())
            || commandToTreasure.equals(PlayerCommand.COLLECT.toString())));

        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

        sendMessageToSocketChannel("", MessageType.DIALOG_END, playerClient); // tells client dialog ended
        if (commandToTreasure.equals(PlayerCommand.CONSUME.toString())) {
            return gameEngine.heroTryConsumingTreasure(playerHero, treasure);
        } else {
            return gameEngine.collectTreasureToHeroBackpack(treasure, playerHero);
        }
    }

    private String playerInteractWithAnotherHero(SocketChannel playerClient, Hero otherHero) throws IOException {
        Hero initiatorHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);
        sendMessageToSocketChannel(String.format(PLAYER_INTERACTION_MESSAGE, otherHero.getName()),
            MessageType.DIALOGING, playerClient);

        String commandToInteractString;
        do {
            commandToInteractString = receiveMessageFromSocketChannel(playerClient);
        } while (!(commandToInteractString.equals(PlayerCommand.ATTACK.toString())
            || commandToInteractString.equals(PlayerCommand.TRADE.toString())));

        sendMessageToSocketChannel("", MessageType.DIALOG_END, playerClient);
        if (commandToInteractString.equals(PlayerCommand.ATTACK.toString())) {
            //TODO sendMessageToSocketChannel("you are being attacked", MessageType.MAP,
            //playersConnectionStorage);
            return gameEngine.battleWithAnotherHero(initiatorHero, otherHero);
        } else {
            int treasureIndex = getTreasureInBackpackIndexFromPlayer(playerClient);
            if (treasureIndex == -1) { // cancelled
                sendMessageToSocketChannel("", MessageType.DIALOG_END, playerClient);
                return null;
            }
            return gameEngine.tradeTreasureWithAnotherHero(initiatorHero, otherHero, treasureIndex);
        }
    }

    private int getTreasureInBackpackIndexFromPlayer(SocketChannel playerClient) throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

        sendMessageToSocketChannel(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE + playerHero.backpack().toString(),
            MessageType.DIALOGING, playerClient);

        String treasureIndexStr;
        int treasureIndex = 0;
        do {
            treasureIndexStr = receiveMessageFromSocketChannel(playerClient);
            if (treasureIndexStr.equals(PlayerCommand.CANCEL.toString())) {
                return -1;
            }

            try {
                treasureIndex = Integer.parseInt(treasureIndexStr.trim());
            } catch (NumberFormatException e) {
                sendMessageToSocketChannel(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE, MessageType.DIALOGING, playerClient);
            }
        } while (treasureIndex <= 0 || treasureIndex > playerHero.backpack().size());

        sendMessageToSocketChannel("", MessageType.DIALOG_END, playerClient);
        return treasureIndex - 1;
    }

    private void displayNewGameFrameToPlayers() throws IOException {
        for (SocketChannel playerClient : playersConnectionStorage.getPlayersSocketChannels()) {
            Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

            if (playerHero.isAlive()) {
                displayMapToPlayerClient(playerClient);
                displayHeroInfoToPlayerClient(playerClient, playerHero);
            } else {
                sendMessageToSocketChannel(DEAD_PLAYER_MESSAGE, MessageType.MESSAGE, playerClient);
                sendMessageToSocketChannel(DISCONNECT_MESSAGE, MessageType.MESSAGE, playerClient);
                playerClient.close();
            }
        }
    }

    private void displayMapToPlayerClient(SocketChannel playerClient) throws IOException {
        sendMessageToSocketChannel(gameEngine.map(), MessageType.MAP, playerClient);
    }

    private void displayHeroInfoToPlayerClient(SocketChannel playerClient, Hero hero) throws IOException {
        sendMessageToSocketChannel(hero.toString(), MessageType.MESSAGE, playerClient);
    }

    private void sendMessageToSocketChannel(String message, MessageType messageType, SocketChannel socketChannel)
        throws IOException {
        networkServer.writeToClient(message, socketChannel);
        networkServer.writeToClient(messageType.toString(), socketChannel);
    }

    private String receiveMessageFromSocketChannel(SocketChannel socketChannel) throws IOException {
        String message;
        do {
            message = networkServer.readFromClient(socketChannel);
        } while (message == null);

        return message;
    }
}
