package bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.exceptions.MaxNumberOfPlayersReachedException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlayersConnectionStorageTest {

    private static final int MAX_NUMBER_OF_PLAYERS = 5;
    private static final PlayersConnectionStorage testPlayersConnectionStorage =
        PlayersConnectionStorage.getInstance(MAX_NUMBER_OF_PLAYERS);

    private static final Hero testHero = new Hero("test hero", new Stats(1, 1, 1, 1));
    private static SocketChannel testSocketChannel;

    @BeforeClass
    public static void setup() throws IOException {
        testSocketChannel = SocketChannel.open();
    }

    @After
    public void disconnectTestSocketChannel() {
        testPlayersConnectionStorage.disconnectPlayer(testSocketChannel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectPlayerWithNullSocketChannelArgument() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.connectPlayer(null, testHero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectPlayerWithNullHeroArgument() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.connectPlayer(testSocketChannel, null);
    }

    @Test(expected = MaxNumberOfPlayersReachedException.class)
    public void testConnectPlayerWithMaxNumberOfPlayersReached() throws IOException,
        MaxNumberOfPlayersReachedException {
        List<SocketChannel> connectedSocketChannels = new ArrayList<>();
        try {
            for (int i = 0; i <= MAX_NUMBER_OF_PLAYERS + 1; ++i) {
                SocketChannel socketChannel = SocketChannel.open();
                connectedSocketChannels.add(socketChannel);

                testPlayersConnectionStorage.connectPlayer(socketChannel, testHero);
            }
        } finally {
            for (SocketChannel connectedSocketChannel : connectedSocketChannels) {
                testPlayersConnectionStorage.disconnectPlayer(connectedSocketChannel);
            }
        }
    }

    @Test
    public void testConnectPlayerSuccess() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        assertTrue("connectPlayer must add player to connected players",
            testPlayersConnectionStorage.isPlayerConnected(testSocketChannel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisconnectPlayerWithNullArgument() {
        testPlayersConnectionStorage.disconnectPlayer(null);
    }

    @Test
    public void testDisconnectPlayerSuccess() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.disconnectPlayer(testSocketChannel); //should do nothing, that's okay

        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        testPlayersConnectionStorage.disconnectPlayer(testSocketChannel);

        assertFalse("disconnectPlayer must remove player from connected players",
            testPlayersConnectionStorage.isPlayerConnected(testSocketChannel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPlayerHeroWithNullArgument() {
        testPlayersConnectionStorage.playerHero(null);
    }

    @Test
    public void testGetPlayerHeroSuccess() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        assertEquals("getPlayerHero must return correct hero associated with player's socket channel",
            testHero, testPlayersConnectionStorage.playerHero(testSocketChannel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPlayerConnectedPlayerHeroWithNullArgument() {
        testPlayersConnectionStorage.isPlayerConnected(null);
    }

    @Test
    public void testIsPlayerConnectedSuccess() throws MaxNumberOfPlayersReachedException {
        assertFalse(testPlayersConnectionStorage.isPlayerConnected(testSocketChannel));

        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        assertTrue(testPlayersConnectionStorage.isPlayerConnected(testSocketChannel));
    }

    @Test
    public void testRemovePlayersWithInterruptedConnectionSuccess()
        throws IOException, MaxNumberOfPlayersReachedException {
        SocketChannel toRemainOpenSocketChannel = SocketChannel.open();
        SocketChannel toBeClosedSocketChannel = SocketChannel.open();

        testPlayersConnectionStorage.connectPlayer(toRemainOpenSocketChannel, testHero);
        testPlayersConnectionStorage.connectPlayer(toBeClosedSocketChannel, testHero);

        toBeClosedSocketChannel.close();

        testPlayersConnectionStorage.removePlayersWithInterruptedConnection();

        assertEquals("removePlayersWithInterruptedConnection must remove closed socket channels",
            Set.of(toRemainOpenSocketChannel), testPlayersConnectionStorage.getPlayersSocketChannels());
    }

    @Test
    public void testGetPlayerHeroByHeroSymbol() throws MaxNumberOfPlayersReachedException, IOException {
        Hero newHero = new Hero("", new Stats(1 , 1, 1, 1));
        SocketChannel newSocketChannel = SocketChannel.open();
        testPlayersConnectionStorage.connectPlayer(newSocketChannel, newHero);
        newHero.setSymbolToVisualize(5);

        assertEquals(newHero, testPlayersConnectionStorage.getPlayerHeroByHeroSymbol('5'));
    }
}
