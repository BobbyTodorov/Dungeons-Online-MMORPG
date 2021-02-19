package bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.exceptions.MaxNumberOfPlayersReachedException;
import org.junit.After;
import org.junit.Before;
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

    @Before
    public void connectTestSocketChannel() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);
    }

    @After
    public void disconnectTestSocketChannel() {
        testPlayersConnectionStorage.disconnectPlayerClient(testSocketChannel);
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

                testPlayersConnectionStorage.connectPlayer(socketChannel,
                    new Hero(testHero.getName(), testHero.getStats()));

                connectedSocketChannels.add(socketChannel);
            }
        } finally {
            for (SocketChannel connectedSocketChannel : connectedSocketChannels) {
                testPlayersConnectionStorage.disconnectPlayerClient(connectedSocketChannel);
            }
        }
    }

    @Test
    public void testConnectPlayerSuccess() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        assertTrue("connectPlayer must add player to connected players",
            testPlayersConnectionStorage.isPlayerClientConnected(testSocketChannel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisconnectPlayerWithNullArgument() {
        testPlayersConnectionStorage.disconnectPlayerClient(null);
    }

    @Test
    public void testDisconnectPlayerSuccess() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.disconnectPlayerClient(testSocketChannel); //should do nothing, that's okay

        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        testPlayersConnectionStorage.disconnectPlayerClient(testSocketChannel);

        assertFalse("disconnectPlayer must remove player from connected players",
            testPlayersConnectionStorage.isPlayerClientConnected(testSocketChannel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPlayerHeroWithNullArgument() {
        testPlayersConnectionStorage.getPlayerHeroOfGivenPlayerClient(null);
    }

    @Test
    public void testGetPlayerHeroSuccess() throws MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        assertEquals("getPlayerHero must return correct hero associated with player's socket channel",
            testHero, testPlayersConnectionStorage.getPlayerHeroOfGivenPlayerClient(testSocketChannel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPlayerConnectedPlayerHeroWithNullArgument() {
        testPlayersConnectionStorage.isPlayerClientConnected(null);
    }

    @Test
    public void testIsPlayerConnectedSuccess() throws MaxNumberOfPlayersReachedException, IOException {
        assertFalse(testPlayersConnectionStorage.isPlayerClientConnected(SocketChannel.open()));

        testPlayersConnectionStorage.connectPlayer(testSocketChannel, testHero);

        assertTrue(testPlayersConnectionStorage.isPlayerClientConnected(testSocketChannel));
    }

    @Test
    public void testRemovePlayersWithInterruptedConnectionSuccess()
        throws IOException, MaxNumberOfPlayersReachedException {
        testPlayersConnectionStorage.disconnectPlayerClient(testSocketChannel); //from @Before

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
