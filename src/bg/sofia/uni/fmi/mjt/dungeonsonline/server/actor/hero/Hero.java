package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.BaseActor;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Position;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Coordinate;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Spell;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Weapon;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public final class Hero extends BaseActor implements IHero {

    private final static int START_HEALTH_POINTS = 100;
    private final static int START_MANA_POINTS = 100;
    private final static int START_ATTACK_POINTS = 50;
    private final static int START_DEFENSE_POINTS = 50;
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

    private static int numberOfInstances = 0;
    private int symbolToVisualize;

    public Hero(String name) {
        super(name);
        this.stats = getStartStats();
        this.level = START_LEVEL;
        this.weapon = START_WEAPON;
        this.spell = START_SPELL;
        positionOnMap = new Position(new Coordinate(0,0));
        this.backpack = new Backpack();
        numberOfInstances++;
        symbolToVisualize = numberOfInstances;
    }

    @Override
    public Backpack getBackpack() {
        return backpack;
    }

    @Override
    public void collectTreasure(Treasure treasure) {
        ArgumentValidator.checkForNullArguments(treasure);

        backpack.addTreasure(treasure);
    }

    @Override
    public void gainExperience(int amountOfExperience) {
        ArgumentValidator.checkForPositiveArguments(amountOfExperience);

        this.experience += amountOfExperience;

        tryLevelingUp();
    }

    @Override
    public void takeHealing(int healingPoints) {
        if (stats.getCurrentHealth() <= 0)  {
            return;
        }

        stats.increaseCurrentHealth(healingPoints);
    }

    @Override
    public void takeMana(int manaPoints) {
        stats.increaseCurrentMana(manaPoints);
    }

    @Override
    public boolean equip(Weapon weapon) {
        ArgumentValidator.checkForNullArguments(weapon);

        if (weapon.getLevel() > level) {
            return false;
        }

        if(getWeapon() == null || weapon.getDamage() > getWeapon().getDamage()) {
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

        if(getSpell() == null || spell.getDamage() > getSpell().getDamage()) {
            this.spell = spell;
            return true;
        }
        return false;
    }

    public void setPositionOnMap(Coordinate coordinate) {
        this.positionOnMap.setCoordinate(coordinate);
    }

    public Position getPositionOnMap() {
        return this.positionOnMap;
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

    private Stats getStartStats() {
        return new Stats(
            START_HEALTH_POINTS,
            START_MANA_POINTS,
            START_ATTACK_POINTS,
            START_DEFENSE_POINTS
        );
    }

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
            //TODO ", backpack=" + backpack +
            ", experience=" + experience +
            '}';
    }
}
