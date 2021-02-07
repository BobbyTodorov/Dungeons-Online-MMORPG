package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.potion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class HealthPotionTest {

    private final static Hero testHero = new Hero("", new Stats(1, 1, 1, 1));


    @Test(expected = IllegalArgumentException.class)
    public void testCreateHealthPotionBySizeWithNullArgument() {
        HealthPotion.createHealthPotionBySize(null);

    }

    @Test
    public void testCreateHealthPotionBySizeSuccess() {
        assertEquals("createHealthPotionBySize must return regular size of health potion",
            50, Objects.requireNonNull(HealthPotion.createHealthPotionBySize(PotionSize.REGULAR)).heal());

        assertEquals("createHealthPotionBySize must return greater size of health potion",
            100, Objects.requireNonNull(HealthPotion.createHealthPotionBySize(PotionSize.GREATER)).heal());

        assertEquals("createHealthPotionBySize must return superior size of health potion",
            200, Objects.requireNonNull(HealthPotion.createHealthPotionBySize(PotionSize.SUPERIOR)).heal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectWithNullArgument() {
        HealthPotion healthPotion = HealthPotion.createHealthPotionBySize(PotionSize.REGULAR);

        //TODO healthPotion.collect()
    }

}
