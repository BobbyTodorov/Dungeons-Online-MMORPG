package dungeonsonline.server.treasure.potion;

import dungeonsonline.server.actor.hero.Hero;
import dungeonsonline.server.validator.ArgumentValidator;

public final class HealthPotion extends BasePotion {

    private final static String CONSUMED_MESSAGE = "%s health points added to your hero!";

    private final static int REGULAR_AMOUNT_TO_HEAL = 50;
    private final static int GREATER_AMOUNT_TO_HEAL = 100;
    private final static int SUPERIOR_AMOUNT_TO_HEAL = 200;


    private HealthPotion(int healingPoints) {
        super(healingPoints);
    }

    public static HealthPotion createHealthPotionBySize(PotionSize potionSize) {
        ArgumentValidator.checkForNullArguments(potionSize);

        switch (potionSize) {
            case REGULAR -> {
                return new HealthPotion(REGULAR_AMOUNT_TO_HEAL);
            }
            case GREATER -> {
                return new HealthPotion(GREATER_AMOUNT_TO_HEAL);
            }
            case SUPERIOR -> {
                return new HealthPotion(SUPERIOR_AMOUNT_TO_HEAL);
            }
            default -> {
                return null;
            }
        }
    }

    public String use(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        hero.takeHealing(heal());
        return String.format(CONSUMED_MESSAGE, heal());
    }

    @Override
    public String toString() {
        return "Health Potion{" + super.toString() + "}";
    }
}
