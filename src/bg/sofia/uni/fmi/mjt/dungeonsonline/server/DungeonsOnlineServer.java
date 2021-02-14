package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Backpack;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.gameengine.GameEngine;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.ClientRequest;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.NetworkServer;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.PlayersConnectionStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.exceptions.MaxNumberOfPlayersReachedException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DungeonsOnlineServer {

    private static final int MAX_NUMBER_OF_PLAYERS_CONNECTED = 9;

    private static final int PLAYER_STARTING_HEALTH = 100;
    private static final int PLAYER_STARTING_MANA = 100;
    private static final int PLAYER_STARTING_ATTACK = 50;
    private static final int PLAYER_STARTING_DEFENSE = 50;

    private static final String ENTER_YOUR_NAME_MESSAGE = "Enter your name: ";
    private static final String GAME_ON_MESSAGE = "Game On!";
    private static final String DISCONNECT_MESSAGE = System.lineSeparator() + "You are being disconnected.";
    private static final String WRONG_COMMAND_MESSAGE = "Wrong command.";
    private static final String UNKNOWN_COMMAND = "Unknown command.";

    private static final String TREASURE_INTERACTION_MESSAGE = "%s found. c (consume) / b (to backpack).";
    private static final String PLAYER_INTERACTION_MESSAGE =
        "interacting with %s, a (attack) / t (trade from backpack).";

    private static final String CHOOSE_INDEX_FROM_BACKPACK_MESSAGE = "Please choose treasure by index or 'cancel'.";
    private static final String BACKPACK_COMMANDS_MESSAGE = "Please choose: u (use) / d (drop)";

    private static final String DEAD_PLAYER_MESSAGE = "You died!";

    private static final String MAX_NUMBER_OF_PLAYERS_REACHED = "Max number of players reached. Please try later.";

    private static final NetworkServer networkServer = NetworkServer.getInstance();
    private static final PlayersConnectionStorage playersConnectionStorage = PlayersConnectionStorage.getInstance(MAX_NUMBER_OF_PLAYERS_CONNECTED);
    private static final GameEngine gameEngine = GameEngine.getInstance();
    private static final StaticObjectsStorage staticObjectStorage = StaticObjectsStorage.getInstance();

    public static void main(String[] args) throws IOException {
        new DungeonsOnlineServer().start();
    }

    public void start() throws IOException {
        networkServer.start();
        while(true) {
            processPlayerRequest(networkServer.pollClientRequest());
        }
    }

    private void processPlayerRequest(ClientRequest playerRequest) throws IOException {
        if (playerRequest == null) {
            return;
        }

        PlayerCommand command = PlayerCommand.fromString(playerRequest.command());

        SocketChannel playerClient = playerRequest.client();

        try {
            String resultOfCommandExecution = executeCommandForPlayer(command, playerClient);

            if (resultOfCommandExecution != null) {
                networkServer.writeToClient(resultOfCommandExecution, playerClient);
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

        return null; //in case of endPlayerSession
    }

    private String startPlayerSession(SocketChannel client) throws IOException {
        networkServer.writeToClient(ENTER_YOUR_NAME_MESSAGE, client);

        String heroName;
        do {
            heroName = networkServer.readFromClient(client);
        } while (heroName == null);

        Hero newHero = new Hero(heroName, getStartingPlayerStats());

        try {
            playersConnectionStorage.connectPlayer(client, newHero);
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
        networkServer.writeToClient(DISCONNECT_MESSAGE, playerClient);

        gameEngine.unSummonPlayerHero(playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient));
        playersConnectionStorage.disconnectPlayerClient(playerClient);
        playerClient.close();
    }

    private String executeCommandOnPlayerBackpack(SocketChannel playerClient) throws IOException {
        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);
        Backpack heroBackpack = playerHero.backpack();

        int treasureIndex = getTreasureInBackpackIndexFromPlayer(playerClient);
        if (treasureIndex == -1) { // cancelled
            return null;
        }

        Treasure treasure = heroBackpack.remove(treasureIndex);

        networkServer.writeToClient(BACKPACK_COMMANDS_MESSAGE, playerClient);

        String commandToTreasureString;
        do {
            commandToTreasureString = networkServer.readFromClient(playerClient);
        } while (commandToTreasureString == null
            || !(commandToTreasureString.equals(PlayerCommand.DROP.toString()) ||
                 commandToTreasureString.equals(PlayerCommand.USE.toString())));

        PlayerCommand commandToTreasure = PlayerCommand.fromString(commandToTreasureString);
        return gameEngine.executeCommandOnHeroTreasure(commandToTreasure, playerHero, treasure);
    }

    private int getTreasureInBackpackIndexFromPlayer(SocketChannel playerClient) throws IOException {
        networkServer.writeToClient(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE, playerClient);

        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

        networkServer.writeToClient(playerHero.backpack().toString(), playerClient);

        String treasureIndexStr;
        int treasureIndex = 0;
        do {
            treasureIndexStr = networkServer.readFromClient(playerClient);
            if (treasureIndexStr == null) {
                continue;
            }
            if (treasureIndexStr.equals(PlayerCommand.CANCEL.toString())) {
                return -1;
            }

            try {
                treasureIndex = Integer.parseInt(treasureIndexStr.trim());
            } catch (NumberFormatException e) {
                networkServer.writeToClient(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE, playerClient);
            }
        } while (treasureIndex <= 0 || treasureIndex > playerHero.backpack().size());

        return treasureIndex - 1;
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
        networkServer.writeToClient(String.format(TREASURE_INTERACTION_MESSAGE, treasure.toString()), playerClient);

        String commandToTreasure;
        do {
            commandToTreasure = networkServer.readFromClient(playerClient);
        } while (commandToTreasure == null
            || !(commandToTreasure.equals(PlayerCommand.CONSUME.toString()) ||
                 commandToTreasure.equals(PlayerCommand.COLLECT.toString())));

        Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);
        if (commandToTreasure.equals(PlayerCommand.CONSUME.toString())) {
            return gameEngine.heroTryConsumingTreasure(playerHero, treasure);
        } else {
            return gameEngine.collectTreasureToHeroBackpack(treasure, playerHero);
        }
    }

    private String playerInteractWithAnotherHero(SocketChannel playerClient, Hero otherHero) throws IOException {
        Hero initiatorHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);
        networkServer.writeToClient(String.format(PLAYER_INTERACTION_MESSAGE, otherHero.getName()), playerClient);

        String commandToInteractString;
        do {
            commandToInteractString = networkServer.readFromClient(playerClient);
        } while (commandToInteractString == null
            || !(commandToInteractString.equals(PlayerCommand.ATTACK.toString()) ||
                 commandToInteractString.equals(PlayerCommand.TRADE.toString())));

        if (commandToInteractString.equals(PlayerCommand.ATTACK.toString())) {
            return gameEngine.battleWithAnotherHero(initiatorHero, otherHero);
        } else {
            int treasureIndex = getTreasureInBackpackIndexFromPlayer(playerClient);
            if (treasureIndex == -1) { // cancelled
                return null;
            }
            return gameEngine.tradeTreasureWithAnotherHero(initiatorHero, otherHero, treasureIndex);
        }
    }

    private void displayNewGameFrameToPlayers() throws IOException {
        for (SocketChannel playerClient : playersConnectionStorage.getPlayersSocketChannels()) {
            Hero playerHero = playersConnectionStorage.getPlayerHeroOfGivenPlayerClient(playerClient);

            if (playerHero.isAlive()) {
                displayMapToPlayerClient(playerClient);
                displayHeroInfoToPlayerClient(playerClient, playerHero);
            } else {
                networkServer.writeToClient(DEAD_PLAYER_MESSAGE, playerClient);
                networkServer.writeToClient(DISCONNECT_MESSAGE, playerClient);
                playerClient.close();
            }
        }
    }

    private void displayMapToPlayerClient(SocketChannel playerClient) throws IOException {
        networkServer.writeToClient(gameEngine.getMapToVisualize(), playerClient);
    }

    private void displayHeroInfoToPlayerClient(SocketChannel playerClient, Hero hero) throws IOException {
        networkServer.writeToClient(hero.toString(), playerClient);
    }
}
