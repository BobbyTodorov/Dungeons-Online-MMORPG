package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public final class Weapon extends BaseSkill{

    public Weapon(String name, int damage, int level) {
        super(name, damage, level);
    }

    @Override
    public String collect(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        if (hero.getLevel() < this.level) {
            return CANT_EQUIP_MESSAGE;
        }

        hero.equip(this);
        return "Weapon " + this.name + " Level: " + this.level + " Damage points: " + this.getDamage() + " equipped!";
    }
}
