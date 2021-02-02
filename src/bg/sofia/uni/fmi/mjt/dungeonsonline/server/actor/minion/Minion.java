package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.minion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.BaseActor;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Visualizable;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Spell;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Weapon;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

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

    private final static char SYMBOL_TO_VISUALIZE_ON_MAP = 'M';

    public Minion(String name, int level, Weapon weapon, Spell spell, Stats stats) {
        super(name);

        ArgumentValidator.checkForPositiveArguments(level);
        ArgumentValidator.checkForNullArguments(stats);

        this.level = level;
        this.weapon = weapon;
        this.spell = spell;
        this.stats = stats;
    }

    public static Minion createMinionByDifficultyLevel(MinionDifficultyLevel difficultyLevel) {
        ArgumentValidator.checkForNullArguments(difficultyLevel);

        switch (difficultyLevel) {
            case EASY -> { return createEasyMinion(); }
            case MEDIUM -> { return createMediumMinion(); }
            case HARD -> { return createHardMinion(); }
            case EXTREME -> { return createExtremeMinion(); }
            default -> { return null; }
        }
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

    private static Minion createExtremeMinion() {
        int minionLevel = 10;
        Weapon extremeWeapon = new Weapon("extreme weapon", 150, minionLevel);
        Spell extremeSpell = new Spell("extreme spell", 200, minionLevel, 100);
        return new Minion("extreme minion", 10, extremeWeapon, extremeSpell, calcStatsByLevel(minionLevel));
    }

    @Override
    public int giveExperience() {
        return level * EXPERIENCE_TO_GIVE_PER_LEVEL;
    }

    private static Stats calcStatsByLevel(int level) {
        return new Stats(
            START_HEALTH_POINTS + HEALTH_PER_LEVEL_MULTIPLIER * level,
            START_MANA_POINTS + MANA_PER_LEVEL_MULTIPLIER * level,
            START_ATTACK_POINTS + ATTACK_PER_LEVEL_MULTIPLIER * level,
            START_DEFENSE_POINTS + DEFENSE_PER_LEVEL_MULTIPLIER * level
        );
    }

    @Override
    public char getSymbolToVisualizeOnMap() {
        return SYMBOL_TO_VISUALIZE_ON_MAP;
    }
}
