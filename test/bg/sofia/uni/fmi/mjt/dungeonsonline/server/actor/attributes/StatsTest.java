package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class StatsTest {

    private static Stats testStat;

    @BeforeClass
    public static void initializeTestStat() {
        testStat = new Stats(1, 1, 1, 1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConstructingWithNonPositiveHealthPoints() {
        new Stats(0, 1, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructingWithNegativeManaPoints() {
        new Stats(1, -1, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructingWithNegativeAttackPoints() {
        new Stats(1, 1, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructingWithNegativeDefensePoints() {
        new Stats(1, 1, 1, -1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIncreaseMaxHealthWithNonPositiveHealthPoints() {
        testStat.increaseMaxHealth(0);
    }

    @Test
    public void testIncreaseMaxHealthSuccess() {
        Stats testStat = new Stats(1, 1, 1, 1);
        testStat.increaseMaxHealth(10);

        assertEquals("increaseMaxHealth does not increase health correctly", 11, testStat.getMaxHealth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncreaseMaxManaWithNegativeManaPoints() {
        testStat.increaseMaxHealth(-1);
    }

    @Test
    public void testIncreaseMaxManaSuccess() {
        Stats testStat = new Stats(1, 1, 1, 1);
        testStat.increaseMaxMana(10);

        assertEquals("increaseMaxMana does not increase health correctly", 11, testStat.getMaxMana());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncreaseCurrentHealthWithNegativeHealthPoints() {
        testStat.increaseCurrentHealth(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncreaseCurrentManaWithNegativeManaPoints() {
        testStat.increaseCurrentMana(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecreaseCurrentHealthWithNegativeHealthPoints() {
        testStat.decreaseCurrentHealth(-1);
    }

    @Test
    public void testDecreaseCurrentHealthBelowZero() {
        testStat.decreaseCurrentHealth(10);

        assertEquals("decreaseCurrentHealth should not decrease health points below 0",
            0, testStat.getCurrentHealth());
    }

    @Test
    public void testDecreaseCurrentHealthSuccess() {
        Stats testStat = new Stats(10, 1, 1, 1);
        testStat.decreaseCurrentHealth(5);

        assertEquals("decreaseCurrentHealth should decrease current health points correctly",
            5, testStat.getCurrentHealth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecreaseCurrentManaWithNegativeManaPoints() {
        testStat.decreaseCurrentMana(-1);
    }

    @Test
    public void testDecreaseCurrentManaBelowZero() {
        testStat.decreaseCurrentMana(10);

        assertEquals("decreaseCurrentMana should not decrease mana points below 0",
            0, testStat.getCurrentMana());
    }

    @Test
    public void testDecreaseCurrentManaSuccess() {
        Stats testStat = new Stats(1, 10, 1, 1);
        testStat.decreaseCurrentMana(5);

        assertEquals("decreaseCurrentMana should decrease current mana points correctly",
            5, testStat.getCurrentMana());
    }

    @Test
    public void testIncreaseCurrentHealthPointsOverHealthManaPoints() {
        Stats testStat = new Stats(10, 1, 1, 1);
        testStat.decreaseCurrentHealth(5);
        testStat.increaseCurrentHealth(10);

        assertEquals("increaseCurrentHealth should not increase health points over max health points",
            10, testStat.getCurrentHealth());
    }

    @Test
    public void testIncreaseCurrentHealthPointsUnderMaxHealthPoints() {
        Stats testStat = new Stats(10, 1, 1, 1);
        testStat.decreaseCurrentHealth(5);
        testStat.increaseCurrentHealth(3);

        assertEquals("increaseCurrentHealth should increase current health points correctly",
            8, testStat.getCurrentHealth());
    }

    @Test
    public void testIncreaseCurrentManaPointsOverMaxManaPoints() {
        Stats testStat = new Stats(1, 10, 1, 1);
        testStat.decreaseCurrentMana(5);
        testStat.increaseCurrentMana(10);

        assertEquals("increaseCurrentMana should not increase mana points over max mana points",
            10, testStat.getCurrentMana());
    }

    @Test
    public void testIncreaseCurrentManaPointsUnderMaxManaPoints() {
        Stats testStat = new Stats(1, 10, 1, 1);
        testStat.decreaseCurrentMana(5);
        testStat.increaseCurrentMana(3);

        assertEquals("increaseCurrentMana should increase current mana points correctly",
            8, testStat.getCurrentMana());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncreaseAttackWithNonPositiveArgument() {
        testStat.increaseAttack(0);
    }

    @Test
    public void testIncreaseAttackSuccess() {
        testStat.increaseAttack(5);

        assertEquals("increaseAttack should increase attack points correctly",
            6, testStat.getAttack());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncreaseDefenseWithNonPositiveArgument() {
        testStat.increaseDefense(0);
    }

    @Test
    public void testIncreaseDefenseSuccess() {
        testStat.increaseDefense(5);

        assertEquals("increaseDefense should increase defense points correctly",
            6, testStat.getDefense());
    }
}
