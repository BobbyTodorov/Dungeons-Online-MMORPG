package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

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

    public Treasure getTreasureAt(int index) {
        ArgumentValidator.checkForNonNegativeArguments(index);

        return treasures.remove(index);
    }

    public int size() {
        return treasures.size();
    }

    public void removeTreasure(Treasure treasure) {
        treasures.remove(treasure);
    }

    @Override
    public String toString() {
        StringBuilder resultString = new StringBuilder("Backpack:\n");
        int treasuresCounter = 1;
        for (Treasure treasure : treasures) {
            resultString.append(treasuresCounter++).append(": ").append(treasure).append('\n');
        }
        return resultString.toString();
    }
}
