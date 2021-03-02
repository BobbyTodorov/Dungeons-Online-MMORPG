package dungeonsonline.server.treasure.skill;

import dungeonsonline.server.actor.hero.Hero;
import dungeonsonline.server.validator.ArgumentValidator;

public final class Spell extends BaseSkill {

    private final static String LEARN_MESSAGE = "Learnt Spell %s Level: %d Damage points: %d, Mana cost: %d";

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
    public String use(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        if (hero.getLevel() < this.level) {
            return String.format(CANT_EQUIP_MESSAGE, "Spell");
        }

        hero.learn(this);
        return String.format(LEARN_MESSAGE, name, level, getDamage(), getManaCost());
    }

    @Override
    public String toString() {
        return "Spell{" +
            super.toString() +
            ", MANA_COST=" + MANA_COST +
            '}';
    }
}
