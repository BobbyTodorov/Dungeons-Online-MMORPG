package dungeonsonline.server.actor.minion;

import dungeonsonline.server.actor.BaseActor;
import dungeonsonline.server.actor.attributes.Stats;
import dungeonsonline.server.map.Visualizable;
import dungeonsonline.server.treasure.skill.Spell;
import dungeonsonline.server.treasure.skill.Weapon;
import dungeonsonline.server.validator.ArgumentValidator;

public class Minion extends BaseActor implements IMinion, Visualizable {

    private final static int EXPERIENCE_TO_GIVE_PER_LEVEL = 10;

    private final static int HEALTH_PER_LEVEL_MULTIPLIER = 20;
    private final static int MANA_PER_LEVEL_MULTIPLIER = 20;
    private final static int ATTACK_PER_LEVEL_MULTIPLIER = 5;
    private final static int DEFENSE_PER_LEVEL_MULTIPLIER = 5;

    private final static int START_HEALTH_POINTS = 30;
    private final static int START_MANA_POINTS = 40;
    private final static int START_ATTACK_POINTS = 10;
    private final static int START_DEFENSE_POINTS = 10;

    public Minion(String name, int level, Weapon weapon, Spell spell, Stats stats) {
        super(name, stats);

        ArgumentValidator.checkForPositiveArguments(level);
        ArgumentValidator.checkForNullArguments(stats);

        this.level = level;
        this.weapon = weapon;
        this.spell = spell;
    }

    /**
     * @param difficultyLevel the difficulty level of the minion to be created
     * @return instance of a Minion with given difficulty level
     */
    public static Minion createMinionByDifficultyLevel(MinionDifficultyLevel difficultyLevel) {
        ArgumentValidator.checkForNullArguments(difficultyLevel);

        switch (difficultyLevel) {
            case EASY -> {
                return createEasyMinion();
            }
            case MEDIUM -> {
                return createMediumMinion();
            }
            case HARD -> {
                return createHardMinion();
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * @return the experience to give when killed
     */
    @Override
    public int giveExperience() {
        return level * EXPERIENCE_TO_GIVE_PER_LEVEL;
    }

    @Override
    public String toString() {
        return "Minion{" +
            "name='" + name + '\'' +
            ", level=" + level +
            ", stats=" + stats +
            ", weapon=" + weapon +
            ", spell=" + spell +
            '}';
    }

    private static Minion createEasyMinion() {
        int minionLevel = 1;
        Spell easySpell = new Spell("easy spell", 15, minionLevel, 10);
        return new Minion("easy minion", minionLevel, null, easySpell, calcStatsByLevel(minionLevel));
    }

    private static Minion createMediumMinion() {
        int minionLevel = 3;
        Weapon mediumWeapon = new Weapon("medium weapon", 30, minionLevel);
        return new Minion("medium minion", minionLevel, mediumWeapon, null, calcStatsByLevel(minionLevel));
    }

    private static Minion createHardMinion() {
        int minionLevel = 5;
        Weapon hardWeapon = new Weapon("hard weapon", 50, minionLevel);
        Spell hardSpell = new Spell("hard spell", 75, minionLevel, 40);
        return new Minion("hard minion", 5, hardWeapon, hardSpell, calcStatsByLevel(minionLevel));
    }

    private static Stats calcStatsByLevel(int level) {
        return new Stats(
            START_HEALTH_POINTS + HEALTH_PER_LEVEL_MULTIPLIER * level,
            START_MANA_POINTS + MANA_PER_LEVEL_MULTIPLIER * level,
            START_ATTACK_POINTS + ATTACK_PER_LEVEL_MULTIPLIER * level,
            START_DEFENSE_POINTS + DEFENSE_PER_LEVEL_MULTIPLIER * level
        );
    }
}
