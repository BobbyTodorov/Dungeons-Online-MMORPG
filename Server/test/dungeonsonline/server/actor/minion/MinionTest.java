package dungeonsonline.server.actor.minion;

import dungeonsonline.server.actor.attributes.Stats;
import dungeonsonline.server.treasure.skill.Spell;
import dungeonsonline.server.treasure.skill.Weapon;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class MinionTest {

    @Test
    public void testCreateMinionByDifficultyLevelEasyMinion() {

        Minion expected = new Minion("easy minion", 1, null,
            new Spell("easy spell", 15, 1, 10),
            new Stats(50, 60, 15, 15));

        assertEquals("createMinionByDifficultyLevel with given EASY must return a new instance of easy minion",
            expected.toString(),
            Objects.requireNonNull(Minion.createMinionByDifficultyLevel(MinionDifficultyLevel.EASY)).toString());
    }

    @Test
    public void testCreateMinionByDifficultyLevelMediumMinion() {

        Minion expected = new Minion("medium minion", 3,
            new Weapon("medium weapon", 30, 3), null,
            new Stats(90, 100, 25, 25));

        assertEquals("createMinionByDifficultyLevel with MEDIUM must return a new instance of medium minion",
            expected.toString(),
            Objects.requireNonNull(Minion.createMinionByDifficultyLevel(MinionDifficultyLevel.MEDIUM)).toString());
    }

    @Test
    public void testCreateMinionByDifficultyLevelHardMinion() {

        Minion expected = new Minion("hard minion", 5,
            new Weapon("hard weapon", 50, 5),
            new Spell("hard spell", 75, 5, 40),
            new Stats(130, 140, 35, 35));

        assertEquals("createMinionByDifficultyLevel with HARD must return a new instance of hard minion",
            expected.toString(),
            Objects.requireNonNull(Minion.createMinionByDifficultyLevel(MinionDifficultyLevel.HARD)).toString());
    }
}
