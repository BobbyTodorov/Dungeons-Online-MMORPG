package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

import java.util.ArrayList;
import java.util.List;

public class Backpack {

    private final List<Treasure> treasures = new ArrayList<>();

    public List<Treasure> getTreasures() {
        return treasures;
    }

    public void addTreasure(Treasure treasureToAdd) {
        ArgumentValidator.checkForNullArguments(treasureToAdd);

        treasures.add(treasureToAdd);
    }

    public Treasure remove(int index) {
        ArgumentValidator.checkForNonNegativeArguments(index);

        return treasures.remove(index);
    }

    public void remove(Treasure treasure) {
        ArgumentValidator.checkForNullArguments(treasure);

        treasures.remove(treasure);
    }

    public int size() {
        return treasures.size();
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
