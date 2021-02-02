package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.hero;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.validator.ArgumentValidator;

import java.util.ArrayList;
import java.util.List;

public final class Backpack {

    private final List<Treasure> treasures = new ArrayList<>();

    public List<Treasure> getTreasures() {
        return treasures;
    }

    public void addTreasure(Treasure treasureToAdd) {
        ArgumentValidator.checkForNullArguments(treasureToAdd);

        treasures.add(treasureToAdd);
    }

    public void removeTreasure(Treasure treasureToRemove) {
        ArgumentValidator.checkForNullArguments(treasureToRemove);

        treasures.remove(treasureToRemove);
    }
}
