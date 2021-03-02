package dungeonsonline.server.treasure.skill;

import dungeonsonline.server.actor.hero.Hero;
import dungeonsonline.server.validator.ArgumentValidator;

public final class Weapon extends BaseSkill {

    private static final String EQUIP_MESSAGE = "Equipped Weapon %s Level: %d Damage points: %d";

    public Weapon(String name, int damage, int level) {
        super(name, damage, level);
    }

    @Override
    public String use(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        if (hero.getLevel() < this.level) {
            return String.format(CANT_EQUIP_MESSAGE, "Weapon");
        }

        hero.equip(this);
        return String.format(EQUIP_MESSAGE, name, level, getDamage());
    }
}
