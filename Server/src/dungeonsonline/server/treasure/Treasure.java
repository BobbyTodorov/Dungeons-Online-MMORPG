package dungeonsonline.server.treasure;

import dungeonsonline.server.actor.hero.Hero;
import dungeonsonline.server.map.Visualizable;

public interface Treasure extends Visualizable {

    char SYMBOL_TO_VISUALIZE_ON_MAP = 'T';

    /**
     * @param hero - the hero to consume the treasure
     * @return status message
     */
    String use(Hero hero);

    default char getSymbolToVisualizeOnMap() {
        return SYMBOL_TO_VISUALIZE_ON_MAP;
    }
}
