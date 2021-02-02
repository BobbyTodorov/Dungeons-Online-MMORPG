package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.map.Visualizable;

public interface Treasure extends Visualizable {

    char SYMBOL_TO_VISUALIZE_ON_MAP = 'T';

    /**
     * @param hero - the hero to collect the treasure
     * @return status message
     */
    String collect(Hero hero);

    default char getSymbolToVisualizeOnMap() {
        return SYMBOL_TO_VISUALIZE_ON_MAP;
    }
}
