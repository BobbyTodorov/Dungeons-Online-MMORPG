package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.skill;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.validator.ArgumentValidator;

public final class Spell extends BaseSkill{
    private final int MANA_COST;

    public Spell(String name, int damage, int level, int manaCost) {
        super(name, damage, level);

        ArgumentValidator.checkForPositiveArguments(manaCost);

        this.MANA_COST = manaCost;
    }

    public int getManaCost() {
        return MANA_COST;
    }

    @Override
    public String collect(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        hero.learn(this);
        return "Spell found! Damage points: " + this.getDamage() + ", Mana cost: " + getManaCost();
    }
}
