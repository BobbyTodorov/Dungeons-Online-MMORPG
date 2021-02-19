package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.potion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public abstract class BasePotion implements Potion {
    private final int POINTS_TO_HEAL;

    protected BasePotion(int pointsToHeal) {
        ArgumentValidator.checkForPositiveArguments(pointsToHeal);

        this.POINTS_TO_HEAL = pointsToHeal;
    }

    public int heal() {
        return POINTS_TO_HEAL;
    }

    @Override
    public String toString() {
        return "Healing Points=" + POINTS_TO_HEAL;
    }
}
