package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.network.NetworkServer;

import java.nio.channels.SocketChannel;

public class DungeonsOnlineServer {

    private static final NetworkServer networkServer = NetworkServer.getInstance();

    private boolean isRunning = false;

    public static void main(String[] args) {
        DungeonsOnlineServer dungeonsOnlineServer = new DungeonsOnlineServer();
        dungeonsOnlineServer.start();
    }

    public void start() {
        isRunning = true;
        networkServer.start();
        analyzeClientsRequests(); //test
    }

    private void analyzeClientsRequests() {
        Pair<SocketChannel, String> newRequest;
        while ((newRequest = networkServer.getClientRequest()) != null) {
            System.out.println(newRequest.getLeftElement() + " from " + newRequest.getRightElement());
        }
    }
}
