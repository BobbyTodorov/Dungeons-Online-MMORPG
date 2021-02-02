package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.skill;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.validator.ArgumentValidator;

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
