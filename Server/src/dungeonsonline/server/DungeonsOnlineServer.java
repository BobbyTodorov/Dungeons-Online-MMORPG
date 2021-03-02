package dungeonsonline.server;

import dungeonsonline.server.actor.hero.Backpack;
import dungeonsonline.server.actor.hero.Hero;
import dungeonsonline.server.actor.hero.movement.Direction;
import dungeonsonline.server.commands.PlayerCommands;
import dungeonsonline.server.gameengine.GameEngine;
import dungeonsonline.server.network.ClientRequest;
import dungeonsonline.server.network.NetworkServer;
import dungeonsonline.server.storage.PlayersConnectionStorage;
import dungeonsonline.server.storage.StaticObjectsStorage;
import dungeonsonline.server.storage.exceptions.MaxNumberOfPlayersReachedException;
import dungeonsonline.server.treasure.Treasure;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DungeonsOnlineServer {

    private static final int MAX_NUMBER_OF_PLAYERS = 9;

    private static final String WRONG_COMMAND_MESSAGE = "Wrong command.";
    private static final String UNKNOWN_COMMAND = "Unknown command.";

    private static final String ENTER_YOUR_NAME_MESSAGE = "Enter your name: ";
    private static final String MAX_NUMBER_OF_PLAYERS_REACHED = "Max number of players reached. Please try later.";
    private static final String GAME_ON_MESSAGE = "Game On!";
    private static final String DISCONNECT_MESSAGE = System.lineSeparator() + "You are being disconnected.";

    private static final String BACKPACK_TREASURE_COMMANDS_MESSAGE =
        "%s" + System.lineSeparator() + "Please choose: use / dr (drop)";
    private static final String TREASURE_INTERACTION_MESSAGE =
        "Treasure %s found. Please choose: use / c (collect).";
    private final static String CHOOSE_INDEX_FROM_BACKPACK_MESSAGE =
        "Please choose treasure by index or 'cancel'." + System.lineSeparator();
    private static final String PLAYER_INTERACTION_MESSAGE =
        "Interacting with player %s. Please choose: a (attack) / t (trade from backpack).";


    private static final String DEAD_PLAYER_MESSAGE = "You died!";
    private static final String PROCESSING_PLAYER_REQUEST_EXCEPTION_MESSAGE = "Processing player request failed.";

    private final ThreadPoolExecutor processor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_NUMBER_OF_PLAYERS);

    private boolean isRunning;

    private final StaticObjectsStorage staticObjectsStorage;
    private final PlayersConnectionStorage playersConnectionStorage;
    private final NetworkServer networkServer;
    private final GameEngine gameEngine;

    public DungeonsOnlineServer(StaticObjectsStorage staticObjectsStorage,
                                PlayersConnectionStorage playersConnectionStorage,
                                NetworkServer networkServer,
                                GameEngine gameEngine) {

        this.staticObjectsStorage = staticObjectsStorage;
        this.playersConnectionStorage = playersConnectionStorage;
        this.networkServer = networkServer;
        this.gameEngine = gameEngine;
    }

    public void start() {
        isRunning = true;
        networkServer.start();

        new Thread(this::run).start();
    }

    public void stop() {
        isRunning = false;
        networkServer.stop();
        processor.shutdown();
    }

    private void run() {

        while (isRunning) {
            ClientRequest playerRequest = networkServer.pollNextClientRequest();

            if (playerRequest == null) {
                continue;
            }

            processor.execute(() -> {
                try {
                    processPlayerRequest(playerRequest);
                } catch (IOException e) {
                    throw new ProcessPlayerRequestFailedException(PROCESSING_PLAYER_REQUEST_EXCEPTION_MESSAGE, e);
                }
            });
        }
    }

    private void processPlayerRequest(ClientRequest playerRequest) throws IOException {
        PlayerCommands command = PlayerCommands.fromString(playerRequest.message());

        SocketChannel playerClient = playerRequest.clientChannel();

        try {
            String resultOfCommandExecution = executeCommandForClient(command, playerClient);

            if (resultOfCommandExecution != null) {
                sendMessageToPlayer(resultOfCommandExecution, playerClient);
            }

            removeDisconnectedPlayersFromGame();
            displayNewGameFrameToPlayers();
        } catch (IOException e) {
            if (playersConnectionStorage.isPlayerClientConnected(playerClient)) {
                executeCommandForClient(PlayerCommands.DISCONNECT, playerClient);
            }
        }
    }

    private String executeCommandForClient(PlayerCommands command, SocketChannel client) throws IOException {

        if (command == null) {
            return WRONG_COMMAND_MESSAGE;
        }

        switch (command) {
            case START -> { return startGameForPlayer(client); }
            case DISCONNECT -> { return endGameForPlayer(client); }
            case BACKPACK -> { return openPlayerBackpack(client); }

            case UP -> { return movePlayerInGivenDirection(client, Direction.UP); }
            case DOWN -> { return movePlayerInGivenDirection(client, Direction.DOWN); }
            case LEFT -> { return movePlayerInGivenDirection(client, Direction.LEFT); }
            case RIGHT -> { return movePlayerInGivenDirection(client, Direction.RIGHT); }

            default -> { return UNKNOWN_COMMAND; }
        }
    }

    private String startGameForPlayer(SocketChannel clientChannel) throws IOException {
        sendMessageToPlayer(ENTER_YOUR_NAME_MESSAGE, clientChannel);

        String heroName = receiveMessageFromPlayer(clientChannel);

        Hero newHero = new Hero(heroName, Hero.startingHeroStats());

        try {
            playersConnectionStorage.connectPlayer(clientChannel, newHero);
        } catch (MaxNumberOfPlayersReachedException e) {
            return MAX_NUMBER_OF_PLAYERS_REACHED;
        }

        gameEngine.summonPlayerHero(newHero);

        return GAME_ON_MESSAGE;
    }

    private String endGameForPlayer(SocketChannel playerClient) throws IOException {
        sendMessageToPlayer(DISCONNECT_MESSAGE, playerClient);

        gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient));
        playersConnectionStorage.disconnectPlayerClient(playerClient);
        playerClient.close();
        return null; // nothing to return (send), player's socket channel is already closed
    }

    private String openPlayerBackpack(SocketChannel playerClient) throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);
        Backpack heroBackpack = playerHero.backpack();

        int treasureIndex = getTreasureInBackpackIndexFromPlayer(playerClient);
        if (treasureIndex == -1) { // cancelled
            return null;
        }

        Treasure treasure = heroBackpack.remove(treasureIndex);

        sendMessageToPlayer(String.format(BACKPACK_TREASURE_COMMANDS_MESSAGE, treasure), playerClient);

        String commandToTreasureString;
        do {
            commandToTreasureString = receiveMessageFromPlayer(playerClient);
        } while (commandToTreasureString == null
                || !(commandToTreasureString.equals(PlayerCommands.USE.toString())
                    || commandToTreasureString.equals(PlayerCommands.DROP.toString())));

        PlayerCommands commandToTreasure = PlayerCommands.fromString(commandToTreasureString);
        if (commandToTreasure == PlayerCommands.USE) {
            return gameEngine.heroTryUsingTreasure(playerHero, treasure);
        } else { // drop
            return gameEngine.dropTreasureFromHero(playerHero, treasure);
        }
    }

    private String movePlayerInGivenDirection(SocketChannel playerClient, Direction direction)
        throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

        String moveHeroResult = gameEngine.moveHero(playerHero, direction);

        if (moveHeroResult.equals(GameEngine.STEP_ON_TREASURE_STATUS)) {
            return interactWithTreasure(playerClient);

        } else if (moveHeroResult.startsWith(GameEngine.STEP_ON_HERO_STATUS)) {
            // Here moveHeroResult = *stepped on hero status string* followed by that hero's symbol.
            char otherHeroSymbol = moveHeroResult.charAt(moveHeroResult.length() - 1);

            Hero otherHero = playersConnectionStorage.getPlayerHeroByHeroSymbol(otherHeroSymbol);
            return interactWithAnotherHero(playerClient, otherHero);
        }

        return moveHeroResult;
    }

    private String interactWithTreasure(SocketChannel playerClient) throws IOException {
        Treasure treasure = staticObjectsStorage.getTreasure();
        sendMessageToPlayer(String.format(TREASURE_INTERACTION_MESSAGE, treasure.toString()), playerClient);

        String commandToTreasureString;
        do {
            commandToTreasureString = receiveMessageFromPlayer(playerClient);
        } while (commandToTreasureString == null
                || !(commandToTreasureString.equals(PlayerCommands.USE.toString())
                    || commandToTreasureString.equals(PlayerCommands.COLLECT.toString())));

        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

        PlayerCommands commandToTreasure = PlayerCommands.fromString(commandToTreasureString);

        if (commandToTreasure == PlayerCommands.USE) {
            return gameEngine.heroTryUsingTreasure(playerHero, treasure);
        } else { // collect
            return gameEngine.collectTreasureToHeroBackpack(treasure, playerHero);
        }
    }

    private String interactWithAnotherHero(SocketChannel playerClient, Hero otherHero) throws IOException {
        Hero initiatorHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);
        sendMessageToPlayer(String.format(PLAYER_INTERACTION_MESSAGE, otherHero.getName()), playerClient);

        String commandToInteractString;
        do {
            commandToInteractString = receiveMessageFromPlayer(playerClient);
        } while (!(commandToInteractString.equals(PlayerCommands.ATTACK.toString())
            || commandToInteractString.equals(PlayerCommands.TRADE.toString())));

        if (commandToInteractString.equals(PlayerCommands.ATTACK.toString())) {
            return gameEngine.battleWithAnotherHero(initiatorHero, otherHero);
        } else {
            int treasureIndex = getTreasureInBackpackIndexFromPlayer(playerClient);
            if (treasureIndex == -1) { // cancelled
                return null;
            }
            return gameEngine.tradeTreasureWithAnotherHero(initiatorHero, otherHero, treasureIndex);
        }
    }

    private int getTreasureInBackpackIndexFromPlayer(SocketChannel playerClient) throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

        sendMessageToPlayer(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE + playerHero.backpack().toString(), playerClient);

        String treasureIndexStr;
        int treasureIndex = 0;
        do {
            treasureIndexStr = receiveMessageFromPlayer(playerClient);
            if (treasureIndexStr.equals(PlayerCommands.CANCEL.toString())) {
                return -1;
            }

            try {
                treasureIndex = Integer.parseInt(treasureIndexStr.trim());
            } catch (NumberFormatException e) {
                sendMessageToPlayer(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE, playerClient);
            }
        } while (treasureIndex <= 0 || treasureIndex > playerHero.backpack().size());

        return treasureIndex - 1;
    }

    private void removeDisconnectedPlayersFromGame() {
        for (SocketChannel playerClient : playersConnectionStorage.getPlayersSocketChannels()) {
            if (!playerClient.isOpen()) {
                gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient));
            }
        }

        playersConnectionStorage.removePlayersWithInterruptedConnection();
    }

    private void displayNewGameFrameToPlayers() throws IOException {
        for (SocketChannel playerClient : playersConnectionStorage.getPlayersSocketChannels()) {
            Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

            if (playerHero.isAlive()) {
                displayMapToPlayerClient(playerClient);
                displayHeroInfoToPlayerClient(playerClient, playerHero);
            } else {
                sendMessageToPlayer(DEAD_PLAYER_MESSAGE, playerClient);
                playerClient.close();
            }
        }
    }

    private void displayMapToPlayerClient(SocketChannel playerClient) throws IOException {
        sendMessageToPlayer(gameEngine.map(), playerClient);
    }

    private void displayHeroInfoToPlayerClient(SocketChannel playerClient, Hero hero) throws IOException {
        sendMessageToPlayer(hero.toString(), playerClient);
    }

    public void sendMessageToPlayer(String message, SocketChannel socketChannel)
        throws IOException {
        networkServer.writeToSocketChannel(message, socketChannel);
    }

    public String receiveMessageFromPlayer(SocketChannel socketChannel) throws IOException {
        String message;
        do {
            message = networkServer.readFromSocketChannel(socketChannel);
        } while (message == null);

        return message;
    }
}
