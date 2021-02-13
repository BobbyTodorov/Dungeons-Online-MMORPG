package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Visualizable;

public interface Treasure extends Visualizable {

    char SYMBOL_TO_VISUALIZE_ON_MAP = 'T';

    /**
     * @param hero - the hero to consume the treasure
     * @return status message
     */
    String consume(Hero hero);

    default char getSymbolToVisualizeOnMap() {
        return SYMBOL_TO_VISUALIZE_ON_MAP;
    }
}
