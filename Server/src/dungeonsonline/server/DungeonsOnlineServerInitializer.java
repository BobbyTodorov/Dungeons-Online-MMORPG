package dungeonsonline.server;

import dungeonsonline.server.gameengine.GameEngine;
import dungeonsonline.server.map.Map;
import dungeonsonline.server.network.NetworkServer;
import dungeonsonline.server.storage.PlayersConnectionStorage;
import dungeonsonline.server.storage.StaticObjectsStorage;

public final class DungeonsOnlineServerInitializer {

    private final static int MAX_NUMBER_OF_PLAYERS = 9;

    public static void main(String[] args) {

        StaticObjectsStorage staticObjectsStorage = new StaticObjectsStorage();
        Map map = new Map(staticObjectsStorage);
        PlayersConnectionStorage playersConnectionStorage = new PlayersConnectionStorage(MAX_NUMBER_OF_PLAYERS);
        NetworkServer networkServer = new NetworkServer();
        GameEngine gameEngine = new GameEngine(staticObjectsStorage, map);

        new DungeonsOnlineServer(staticObjectsStorage, playersConnectionStorage, networkServer, gameEngine).start();
    }
}
