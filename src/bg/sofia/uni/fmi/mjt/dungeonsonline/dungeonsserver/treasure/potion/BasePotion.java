package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.potion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.validator.ArgumentValidator;

public abstract class BasePotion implements Potion{
    private final int POINTS_TO_HEAL;

    protected BasePotion(int pointsToHeal){
        ArgumentValidator.checkForPositiveArguments(pointsToHeal);

        this.POINTS_TO_HEAL = pointsToHeal;
    }

    public int heal(){
        return POINTS_TO_HEAL;
    }
}
