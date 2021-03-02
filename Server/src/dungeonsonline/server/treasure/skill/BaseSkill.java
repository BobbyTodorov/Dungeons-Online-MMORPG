package dungeonsonline.server.treasure.skill;

import dungeonsonline.server.validator.ArgumentValidator;

public abstract class BaseSkill implements Skill {

    protected String name;
    protected int damage;
    protected int level;

    public final static String CANT_EQUIP_MESSAGE = "%s level is too high for you to use.";

    public BaseSkill(String name, int damage, int level) {
        ArgumentValidator.checkForNullArguments(name);
        ArgumentValidator.checkForPositiveArguments(damage, level);

        this.name = name;
        this.damage = damage;
        this.level = level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "name='" + name +
            "', damage=" + damage +
            ", level=" + level;
    }
}
