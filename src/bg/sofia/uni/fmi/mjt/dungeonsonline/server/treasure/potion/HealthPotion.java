package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.potion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public final class HealthPotion extends BasePotion {

    private final static int REGULAR_AMOUNT_TO_HEAL = 50;
    private final static int GREATER_AMOUNT_TO_HEAL = 100;
    private final static int SUPERIOR_AMOUNT_TO_HEAL = 200;


    private HealthPotion(int healingPoints) {
        super(healingPoints);
    }

    public static HealthPotion createHealthPotionBySize(PotionSize potionSize) {
        ArgumentValidator.checkForNullArguments(potionSize);

        switch (potionSize) {
            case REGULAR -> { return new HealthPotion(REGULAR_AMOUNT_TO_HEAL); }
            case GREATER -> { return new HealthPotion(GREATER_AMOUNT_TO_HEAL); }
            case SUPERIOR -> { return new HealthPotion(SUPERIOR_AMOUNT_TO_HEAL); }
            default -> { return null; }
        }
    }

    public String consume(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        hero.takeHealing(this.heal());
        return this.heal() + " health points added to your hero!";
    }

    @Override
    public String toString() {
        return "Health Potion{" + super.toString() + "}";
    }
}
