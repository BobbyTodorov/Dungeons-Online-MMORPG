package dungeonsonline.server.actor.minion;

import dungeonsonline.server.actor.Actor;

public interface IMinion extends Actor {

    char SYMBOL_TO_VISUALIZE_ON_MAP = 'M';

    /**
     * @return experience to be given to the minion's killer.
     */
    int giveExperience();

    default char getSymbolToVisualizeOnMap() {
        return SYMBOL_TO_VISUALIZE_ON_MAP;
    }
}
