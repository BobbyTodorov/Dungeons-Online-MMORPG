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
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.BaseSkill;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DungeonsOnlineServer {

    private static final int MAX_NUMBER_OF_PLAYERS_CONNECTED = 9;

    private static final String ENTER_YOUR_NAME_MESSAGE = "Enter your name: ";
    private static final String GAME_ON_MESSAGE = "Game On!";
    private static final String DISCONNECT_MESSAGE = System.lineSeparator() + "You are being disconnected.";
    private static final String WRONG_COMMAND_MESSAGE = "Wrong command.";

    private static final String TREASURE_INTERACTION_MESSAGE = "%s found. c (consume) / b (to backpack).";
    private static final String PLAYER_INTERACTION_MESSAGE = "interacting with %s, a (attack) / t (trade from backpack).";

    private static final String TREASURE_COLLECTED_TO_BACKPACK_MESSAGE = "%s collected to backpack.";

    private static final String CHOOSE_INDEX_FROM_BACKPACK_MESSAGE = "Please choose treasure by index or 'cancel'.";
    private static final String BACKPACK_FUNCTION_MESSAGE = "Please choose: u (use) / d (drop)";

    private static final String DEAD_PLAYER_MESSAGE = "You died!";

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
        if (command == null) {
            return;
        }

        SocketChannel client = playerRequest.client();

        try {
            String commandResult = executeCommandForPlayer(command, client);

            if (commandResult != null) {
                networkServer.writeToClient(commandResult, client);
            }

            playersConnectionStorage.removePlayersWithInterruptedConnection();
            displayNewGameFrameToPlayers();
        } catch (IOException e) {
            if (playersConnectionStorage.isPlayerConnected(client)) {
                endPlayerSession(client);
            }
        }
    }

    private String executeCommandForPlayer(PlayerCommand command, SocketChannel client) throws IOException {
        switch (command) {
            case START -> { return startPlayerSession(client); }
            case DISCONNECT -> { endPlayerSession(client); }
            case BACKPACK -> { return executeCommandOnPlayerBackpack(client); }

            case UP -> { return executePlayerMoveInGivenDirection(client, Direction.UP); }
            case DOWN -> { return executePlayerMoveInGivenDirection(client, Direction.DOWN); }
            case LEFT -> { return executePlayerMoveInGivenDirection(client, Direction.LEFT); }
            case RIGHT -> { return executePlayerMoveInGivenDirection(client, Direction.RIGHT); }

            default -> { return WRONG_COMMAND_MESSAGE; }
        }

        return null; //in case of endPlayerSession
    }

    private String startPlayerSession(SocketChannel client) throws IOException {
        networkServer.writeToClient(ENTER_YOUR_NAME_MESSAGE, client);

        String heroName;
        do {
            heroName = networkServer.readFromClient(client);
        } while (heroName == null);

        Hero newHero = new Hero(heroName);
        playersConnectionStorage.connectPlayer(client, newHero);
        gameEngine.summonPlayerHero(newHero);

        return GAME_ON_MESSAGE;
    }

    private void endPlayerSession(SocketChannel client) throws IOException {
        networkServer.writeToClient(DISCONNECT_MESSAGE, client);
        gameEngine.unSummonPlayerHero(playersConnectionStorage.playerHero(client));
        playersConnectionStorage.disconnectPlayer(client);
        client.close();
    }

    private String executeCommandOnPlayerBackpack(SocketChannel client) throws IOException {
        Hero hero = playersConnectionStorage.playerHero(client);
        Backpack backpack = hero.backpack();

        int treasureIndex = getTreasureFromBackpackIndexFromPlayer(client);
        if (treasureIndex == -1) { // cancelled
            return null;
        }

        Treasure treasure = backpack.remove(treasureIndex);

        networkServer.writeToClient(BACKPACK_FUNCTION_MESSAGE, client);

        String commandToBackpackStr;
        do {
            commandToBackpackStr = networkServer.readFromClient(client);
        } while (commandToBackpackStr == null
            || !(commandToBackpackStr.equals(PlayerCommand.DROP.toString())
            || commandToBackpackStr.equals(PlayerCommand.USE.toString())));

        PlayerCommand commandToBackpack = PlayerCommand.fromString(commandToBackpackStr);
        return gameEngine.executeCommandOnHeroTreasure(commandToBackpack, hero, treasure);
    }

    private int getTreasureFromBackpackIndexFromPlayer(SocketChannel client) throws IOException {
        networkServer.writeToClient(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE, client);

        Hero playerHero = playersConnectionStorage.playerHero(client);

        networkServer.writeToClient(playerHero.backpack().toString(), client);

        String treasureIndexStr;
        int treasureIndex = 0;
        do {
            treasureIndexStr = networkServer.readFromClient(client);
            if (treasureIndexStr == null) {
                continue;
            }
            if (treasureIndexStr.equals(PlayerCommand.CANCEL.toString())) {
                return -1;
            }

            try {
                treasureIndex = Integer.parseInt(treasureIndexStr.trim());
            } catch (NumberFormatException e) {
                networkServer.writeToClient(CHOOSE_INDEX_FROM_BACKPACK_MESSAGE, client);
            }
        } while (treasureIndex <= 0 || treasureIndex > playerHero.backpack().size());

        return treasureIndex - 1;
    }

    private String executePlayerMoveInGivenDirection(SocketChannel client, Direction direction) throws IOException {
        Hero hero = null;
        if (playersConnectionStorage.isPlayerConnected(client)) {
            hero = playersConnectionStorage.playerHero(client);
        }

        String moveHeroResult = gameEngine.moveHero(hero, direction);

        if (moveHeroResult.equals(GameEngine.STEP_ON_TREASURE_STATUS)) {
            return interactWithTreasure(client);
        }

        if (moveHeroResult.contains(GameEngine.STEP_ON_PLAYER_STATUS)) { //"player" followed by his hero symbol. For example "player 4".
            Hero otherHero = playersConnectionStorage.getPlayerHeroByHeroSymbol((moveHeroResult.split(" ")[1].charAt(0)));
            return interactWithPlayerHero(client, otherHero);
        }

        return moveHeroResult;
    }

    private String interactWithTreasure(SocketChannel client) throws IOException {
        Treasure treasure = staticObjectStorage.getTreasure();
        networkServer.writeToClient(String.format(TREASURE_INTERACTION_MESSAGE, treasure.toString()), client);

        String commandToTreasure;
        do {
            commandToTreasure = networkServer.readFromClient(client);
        } while (commandToTreasure == null
            || !(commandToTreasure.equals(PlayerCommand.CONSUME.toString())
            || commandToTreasure.equals(PlayerCommand.COLLECT.toString())));

        if (commandToTreasure.equals(PlayerCommand.CONSUME.toString())) {
            return clientHeroTryConsumingTreasure(client, treasure);
        } else {
            return addTreasureToClientHeroBackpack(client, treasure);
        }
    }

    private String addTreasureToClientHeroBackpack(SocketChannel client, Treasure item) {
        playersConnectionStorage.playerHero(client).collectTreasure(item);

        return String.format(TREASURE_COLLECTED_TO_BACKPACK_MESSAGE, item.toString());
    }

    private String clientHeroTryConsumingTreasure(SocketChannel client, Treasure treasure) {
        String collectResult = treasure.collect(playersConnectionStorage.playerHero(client));

        if (collectResult.equals(BaseSkill.CANT_EQUIP_MESSAGE)) {
            return collectResult + System.lineSeparator() + addTreasureToClientHeroBackpack(client, treasure);
        }

        return collectResult;
    }

    private String interactWithPlayerHero(SocketChannel client, Hero otherHero) throws IOException {
        Hero interactingHero = playersConnectionStorage.playerHero(client);
        networkServer.writeToClient(String.format(PLAYER_INTERACTION_MESSAGE, otherHero.getName()), client);

        String commandToInteractStr;
        do {
            commandToInteractStr = networkServer.readFromClient(client);
        } while (commandToInteractStr == null
            || !(commandToInteractStr.equals(PlayerCommand.ATTACK.toString())
            || commandToInteractStr.equals(PlayerCommand.TRADE.toString())));

        if (commandToInteractStr.equals(PlayerCommand.ATTACK.toString())) {
            return gameEngine.battleWithPlayer(interactingHero, otherHero);
        } else {
            int treasureIndex = getTreasureFromBackpackIndexFromPlayer(client);
            if (treasureIndex == -1) { // cancelled
                return null;
            }
            return gameEngine.tradeWithPlayer(interactingHero, otherHero, treasureIndex);
        }
    }

    private void displayNewGameFrameToPlayers() throws IOException {
        for (SocketChannel player : playersConnectionStorage.getPlayersSocketChannels()) {
            if (playersConnectionStorage.playerHero(player).isAlive()) {
                networkServer.writeToClient(gameEngine.getMapToVisualize(), player);
                networkServer.writeToClient(playersConnectionStorage.playerHero(player).toString(), player); //TODO proper UI
            } else {
                networkServer.writeToClient(DEAD_PLAYER_MESSAGE, player);
                endPlayerSession(player);
            }
        }
    }
}
