package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.potion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;

public interface Potion extends Treasure {
    /**
     * @return the amount of points to gain
     */
    int heal();
}
