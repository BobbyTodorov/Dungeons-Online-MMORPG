package dungeonsonline.server.actor.hero;

import dungeonsonline.server.treasure.skill.Spell;
import dungeonsonline.server.treasure.skill.Weapon;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public final class BackpackTest {

    Weapon testWeapon = new Weapon("", 1, 1);
    Spell testSpell = new Spell("", 1, 1, 1);

    Backpack testBackpack;

    @Before
    public void refreshBackpack() {
        testBackpack = new Backpack();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTreasureWithNullArgument() {
        new Backpack().addTreasure(null);
    }

    @Test
    public void testAddTreasureSuccess() {
        testBackpack.addTreasure(testWeapon);

        assertEquals("addTreasure must add given treasure to backpack",
            List.of(testWeapon), testBackpack.getTreasures());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTreasureWithNegativeIndex() {
        new Backpack().remove(-1);
    }

    @Test
    public void testRemoveTreasureByIndexSuccess() {
        testBackpack.addTreasure(testWeapon);

        Assert.assertEquals("removeTreasure by index must remove correct treasure",
            testWeapon, testBackpack.remove(0));

        assertEquals("removeTreasure by index from backpack with one item should leave an empty backpack",
            0, testBackpack.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTreasureWithNullTreasure() {
        new Backpack().remove(null);
    }

    @Test
    public void testRemoveTreasureByGivenTreasureSuccess() {
        testBackpack.addTreasure(testWeapon);
        testBackpack.addTreasure(testSpell);
        testBackpack.remove(testWeapon);

        assertEquals("removeTreasure by given treasure should remove the given treasure from backpack",
            List.of(testSpell), testBackpack.getTreasures());
    }

    @Test
    public void testSizeSuccess() {
        assertEquals("size must return 0 if backpack is empty",
            0, testBackpack.size());

        testBackpack.addTreasure(testWeapon);

        assertEquals("size must return 1 if backpack contains one treasure",
            1, testBackpack.size());

        testBackpack.addTreasure(testWeapon);

        assertEquals("size must return 2 if backpack contains two treasures",
            2, testBackpack.size());
    }

}
