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
    private static final Map<Integer, Boolean> playersSymbolsToAvailable = new LinkedHashMap<>();


    private PlayersConnectionStorage(int maxNumberOfPlayers) {
        ArgumentValidator.checkForPositiveArguments(maxNumberOfPlayers);

        this.MAX_NUMBER_OF_PLAYERS = maxNumberOfPlayers;
        for (int i = 1; i <= MAX_NUMBER_OF_PLAYERS; ++i) {
            playersSymbolsToAvailable.put(i, true);
        }
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

    /**
     * Sets the first available symbol to given hero and pairs given socketChannel and hero.
     *
     * @throws MaxNumberOfPlayersReachedException if max number of connected players has been reached
     */
    public void connectPlayer(SocketChannel socketChannel, Hero hero) throws MaxNumberOfPlayersReachedException {
        ArgumentValidator.checkForNullArguments(socketChannel, hero);

        if (playersSocketChannelToHero.size() >= MAX_NUMBER_OF_PLAYERS) {
            throw new MaxNumberOfPlayersReachedException(MAX_NUMBER_OF_PLAYERS_REACHED_EXCEPTION);
        }

        int firstAvailableHeroSymbol = getFirstAvailableHeroSymbol();
        playersSymbolsToAvailable.put(firstAvailableHeroSymbol, false);
        hero.setSymbolToVisualize(firstAvailableHeroSymbol);
        playersSocketChannelToHero.put(socketChannel, hero);
    }

    /**
     * Removes the pair socketChannel - hero, where hero is the one paired with that socketChannel.
     * If there is no such pair this method does nothing.
     */
    public void disconnectPlayerClient(SocketChannel socketChannel) {
        ArgumentValidator.checkForNullArguments(socketChannel);

        if (playersSocketChannelToHero.size() <= 0 || !playersSocketChannelToHero.containsKey(socketChannel)) {
            return;
        }

        playersSymbolsToAvailable
            .put(playersSocketChannelToHero.get(socketChannel).getSymbolToVisualizeOnMap() - '0', true);
        playersSocketChannelToHero.remove(socketChannel);
    }

    public Hero getPlayerHeroOfGivenPlayerClient(SocketChannel socketChannel) {
        ArgumentValidator.checkForNullArguments(socketChannel);

        return playersSocketChannelToHero.get(socketChannel);
    }

    public boolean isPlayerClientConnected(SocketChannel socketChannel) {
        ArgumentValidator.checkForNullArguments(socketChannel);

        return playersSocketChannelToHero.containsKey(socketChannel);
    }

    /**
     * Removes all pairs socketChannel - hero if the socketChannel is not connected anymore/closed.
     */
    public void removePlayersWithInterruptedConnection() {
        for (Map.Entry<SocketChannel, Hero> playerEntry : playersSocketChannelToHero.entrySet()) {
            if (!playerEntry.getKey().isOpen()) {
                playersSymbolsToAvailable.put(playerEntry.getValue().getSymbolToVisualizeOnMap() - '0', true);
            }
        }

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

    private int getFirstAvailableHeroSymbol() {
        for (Map.Entry<Integer, Boolean> entry : playersSymbolsToAvailable.entrySet()) {
            if (entry.getValue()) {
                return entry.getKey();
            }
        }
        return 0;
    }
}
