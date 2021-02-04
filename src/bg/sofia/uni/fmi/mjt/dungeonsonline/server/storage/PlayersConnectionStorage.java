package bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;

import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PlayersConnectionStorage {

    private final int MAX_NUMBER_OF_PLAYERS;

    private static PlayersConnectionStorage instance;

    private static final Map<SocketChannel, Hero> playersSocketChannelToHero = new LinkedHashMap<>();

    private int numberOfPlayers = 0;


    private PlayersConnectionStorage(int maxNumberOfPlayers) {
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

    public void connectPlayer(SocketChannel socketChannel, Hero hero) {
        if (numberOfPlayers > MAX_NUMBER_OF_PLAYERS) {
            //TODO throw
            return;
        }

        numberOfPlayers++;
        playersSocketChannelToHero.put(socketChannel, hero);
    }

    public void disconnectPlayer(SocketChannel socketChannel) {
        numberOfPlayers--;
        playersSocketChannelToHero.remove(socketChannel);
    }

    public Hero getPlayerHero(SocketChannel socketChannel) {
        return playersSocketChannelToHero.get(socketChannel);
    }

    public boolean isPlayerAlreadyConnected(SocketChannel socketChannel) {
        return playersSocketChannelToHero.containsKey(socketChannel);
    }

    public Hero getPlayerHeroByHeroSymbol(char heroSymbol) {
        return playersSocketChannelToHero.values().stream()
            .filter(h -> h.getSymbolToVisualizeOnMap() == heroSymbol)
            .reduce((a, b) -> {
                throw new IllegalStateException("Multiple elements: " + a + ", " + b);
            })
            .orElse(null);
    }
}
