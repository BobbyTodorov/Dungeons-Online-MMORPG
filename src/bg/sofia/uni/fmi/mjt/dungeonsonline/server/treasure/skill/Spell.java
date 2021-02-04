package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

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
        return "Spell " + this.name + " Level: " + this.level + " Damage points: " + this.getDamage() + ", Mana cost: " + getManaCost();
    }

    @Override
    public String toString() {
        return "Spell{" +
            super.toString() +
            " MANA_COST=" + MANA_COST +
            '}';
    }
}
