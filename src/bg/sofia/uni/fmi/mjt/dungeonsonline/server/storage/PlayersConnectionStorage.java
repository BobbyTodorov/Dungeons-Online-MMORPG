package bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.exceptions.MaxNumberOfPlayersReachedException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PlayersConnectionStorage {

    private final static String MAX_NUMBER_OF_PLAYERS_REACHED_EXCEPTION = "Maximum number of players reached.";

    private final int MAX_NUMBER_OF_PLAYERS;

    private static PlayersConnectionStorage instance;

    private static final Map<SocketChannel, Hero> playersSocketChannelToHero = new LinkedHashMap<>();

    private int numberOfPlayers = 0;


    private PlayersConnectionStorage(int maxNumberOfPlayers) {
        ArgumentValidator.checkForPositiveArguments(maxNumberOfPlayers);

        this.MAX_NUMBER_OF_PLAYERS = maxNumberOfPlayers;
    }

    public static PlayersConnectionStorage getInstance(int maxNumberOfPlayers) {
        if (instance == null) {
            instance = new PlayersConnectionStorage(maxNumberOfPlayers);
        }

        return instance;
    }

    public Set<SocketChannel> getPlayersSocketChannels() {
        return playersSocketChannelToHero.keySet();
    }

    public void connectPlayer(SocketChannel socketChannel, Hero hero) throws MaxNumberOfPlayersReachedException {
        ArgumentValidator.checkForNullArguments(socketChannel, hero);

        if (numberOfPlayers > MAX_NUMBER_OF_PLAYERS) {
            throw new MaxNumberOfPlayersReachedException(MAX_NUMBER_OF_PLAYERS_REACHED_EXCEPTION);
        }

        numberOfPlayers++;
        hero.setSymbolToVisualize(numberOfPlayers);
        playersSocketChannelToHero.put(socketChannel, hero);
    }

    public void disconnectPlayer(SocketChannel socketChannel) {
        ArgumentValidator.checkForNullArguments(socketChannel);

        if (playersSocketChannelToHero.containsKey(socketChannel)) {
            numberOfPlayers--;
            playersSocketChannelToHero.remove(socketChannel);
        }
    }

    public Hero playerHero(SocketChannel socketChannel) {
        ArgumentValidator.checkForNullArguments(socketChannel);

        return playersSocketChannelToHero.get(socketChannel);
    }

    public boolean isPlayerConnected(SocketChannel socketChannel) {
        ArgumentValidator.checkForNullArguments(socketChannel);

        return playersSocketChannelToHero.containsKey(socketChannel);
    }

    public void removePlayersWithInterruptedConnection() {
        //TODO numberOfPlayers--
        playersSocketChannelToHero.keySet().removeIf(sc -> !sc.isOpen());
    }

    public Hero getPlayerHeroByHeroSymbol(char heroSymbol) {
        for (Map.Entry<SocketChannel, Hero> entry : playersSocketChannelToHero.entrySet()) {
            Hero entryHero = entry.getValue();
            if (entryHero.getSymbolToVisualizeOnMap() == heroSymbol) {
                return entryHero;
            }
        }

        return null;
    }
}
