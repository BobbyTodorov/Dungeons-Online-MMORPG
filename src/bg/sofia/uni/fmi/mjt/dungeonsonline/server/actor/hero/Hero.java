package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.BaseActor;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Position;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Coordinate;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Spell;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Weapon;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public class Hero extends BaseActor implements IHero {

    private final static int START_LEVEL = 1;
    private final static Weapon START_WEAPON = null;
    private final static Spell START_SPELL = null;

    private final static int HEALTH_INCREASE_PER_LEVEL = 10;
    private final static int MANA_INCREASE_PER_LEVEL = 10;
    private final static int ATTACK_INCREASE_PER_LEVEL = 5;
    private final static int DEFENSE_INCREASE_PER_LEVEL = 5;

    private final static int EXPERIENCE_NEEDED_PER_LEVEL_MULTIPLIER = 20;

    private final Position positionOnMap;
    private final Backpack backpack;
    private int experience;

    private int symbolToVisualize;

    public Hero(String name, Stats stats) {
        super(name, stats);
        this.level = START_LEVEL;
        this.weapon = START_WEAPON;
        this.spell = START_SPELL;
        positionOnMap = new Position(new Coordinate(0, 0));
        this.backpack = new Backpack();
    }

    /**
     * @param symbolToVisualize the symbol associated to this hero, to be visualized later on the map
     */
    public void setSymbolToVisualize(int symbolToVisualize) {
        this.symbolToVisualize = symbolToVisualize;
    }

    @Override
    public Backpack backpack() {
        return backpack;
    }

    public Position positionOnMap() {
        return this.positionOnMap;
    }

    public void setPositionOnMap(Coordinate coordinate) {
        this.positionOnMap.setCoordinate(coordinate);
    }

    @Override
    public void collectTreasure(Treasure treasure) {
        ArgumentValidator.checkForNullArguments(treasure);

        backpack.addTreasure(treasure);
    }

    @Override
    public int gainExperience(int amountOfExperience) {
        ArgumentValidator.checkForPositiveArguments(amountOfExperience);

        this.experience += amountOfExperience;

        tryLevelingUp();

        return this.experience;
    }

    @Override
    public void takeHealing(int healingPoints) {
        ArgumentValidator.checkForNonNegativeArguments(healingPoints);

        if (healingPoints == 0) {
            return;
        }

        if (stats.getCurrentHealth() <= 0) {
            return;
        }

        stats.increaseCurrentHealth(healingPoints);
    }

    @Override
    public void takeMana(int manaPoints) {
        ArgumentValidator.checkForNonNegativeArguments(manaPoints);

        if (manaPoints == 0) {
            return;
        }

        stats.increaseCurrentMana(manaPoints);
    }

    @Override
    public boolean equip(Weapon weapon) {
        ArgumentValidator.checkForNullArguments(weapon);

        if (weapon.getLevel() > level) {
            return false;
        }

        if (getWeapon() == null || weapon.getDamage() > getWeapon().getDamage()) {
            this.weapon = weapon;
            return true;
        }
        return false;
    }

    @Override
    public boolean learn(Spell spell) {
        ArgumentValidator.checkForNullArguments(spell);

        if (spell.getLevel() > level) {
            return false;
        }

        if (getSpell() == null || spell.getDamage() > getSpell().getDamage()) {
            this.spell = spell;
            return true;
        }
        return false;
    }

    private void tryLevelingUp() {
        int totalExperienceForLevelUp = EXPERIENCE_NEEDED_PER_LEVEL_MULTIPLIER * level;
        if (experience >= totalExperienceForLevelUp) {
            levelUp();
            experience = Math.abs(totalExperienceForLevelUp - experience);
        }
    }

    private void levelUp() {
        level++;

        stats.increaseMaxHealth(HEALTH_INCREASE_PER_LEVEL);
        stats.increaseCurrentMana(MANA_INCREASE_PER_LEVEL);
        stats.increaseAttack(ATTACK_INCREASE_PER_LEVEL);
        stats.increaseDefense(DEFENSE_INCREASE_PER_LEVEL);
    }

    /**
     * @return the symbol associated to this hero that is to be visualized on the map
     */
    @Override
    public char getSymbolToVisualizeOnMap() {
        return (char) (symbolToVisualize + '0');
    }

    @Override
    public String toString() {
        return "Hero " + getSymbolToVisualizeOnMap() +
            " {name=" + name +
            ", level=" + level +
            ", stats=" + stats +
            ", weapon=" + weapon +
            ", spell=" + spell +
            ", experience=" + experience +
            '}';
    }
}
