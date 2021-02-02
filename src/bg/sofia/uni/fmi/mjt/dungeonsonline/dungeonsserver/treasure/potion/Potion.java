package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.potion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.Treasure;

public interface Potion extends Treasure {
    /**
     * @return the amount of points to gain
     */
    int heal();
}
