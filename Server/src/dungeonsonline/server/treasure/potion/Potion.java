package dungeonsonline.server.treasure.potion;

import dungeonsonline.server.treasure.Treasure;

public interface Potion extends Treasure {
    /**
     * @return the amount of points to gain
     */
    int heal();
}
