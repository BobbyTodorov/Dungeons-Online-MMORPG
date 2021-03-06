package dungeonsonline.server.treasure.potion;

import dungeonsonline.server.actor.hero.Hero;
import dungeonsonline.server.validator.ArgumentValidator;

public final class ManaPotion extends BasePotion {

    private final static String CONSUMED_MESSAGE = "%s mana points added to your hero!";

    private final static int REGULAR_AMOUNT_TO_RECHARGE = 50;
    private final static int GREATER_AMOUNT_TO_RECHARGE = 90;
    private final static int SUPERIOR_AMOUNT_TO_RECHARGE = 150;

    public ManaPotion(int manaPoints) {
        super(manaPoints);
    }

    public static ManaPotion createManaPotionBySize(PotionSize potionSize) {
        ArgumentValidator.checkForNullArguments(potionSize);

        switch (potionSize) {
            case REGULAR -> {
                return new ManaPotion(REGULAR_AMOUNT_TO_RECHARGE);
            }
            case GREATER -> {
                return new ManaPotion(GREATER_AMOUNT_TO_RECHARGE);
            }
            case SUPERIOR -> {
                return new ManaPotion(SUPERIOR_AMOUNT_TO_RECHARGE);
            }
            default -> {
                return null;
            }
        }
    }

    public String use(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        hero.takeMana(heal());
        return String.format(CONSUMED_MESSAGE, heal());
    }

    @Override
    public String toString() {
        return "Mana Potion{" + super.toString() + "}";
    }
}
