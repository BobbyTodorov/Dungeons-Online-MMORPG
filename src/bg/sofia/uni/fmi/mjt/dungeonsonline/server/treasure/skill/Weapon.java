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

        hero.equip(this);
        return "Weapon found! Damage points: " + this.getDamage();
    }
}