package dungeonsonline.server;

import dungeonsonline.server.gameengine.GameEngine;
import dungeonsonline.server.network.NetworkServer;
import dungeonsonline.server.storage.PlayersConnectionStorage;
import dungeonsonline.server.storage.StaticObjectsStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DungeonsOnlineServerTest {

    @Mock
    StaticObjectsStorage staticObjectsStorageMock;

    @Mock
    PlayersConnectionStorage playersConnectionStorageMock;

    @Mock
    NetworkServer networkServerMock;

    @Mock
    GameEngine gameEngine;

    @InjectMocks
    DungeonsOnlineServer testDungeonsOnlineServer;

    @Test
    public void testStartSuccess() {
        testDungeonsOnlineServer.start();

        verify(networkServerMock).start();
    }

    @Test
    public void testStopSuccess() {
        testDungeonsOnlineServer.stop();

        verify(networkServerMock).stop();
    }
}
