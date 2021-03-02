package dungeonsonline.server.treasure.potion;

import dungeonsonline.server.actor.hero.Hero;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HealthPotionTest {

    @Mock
    Hero heroMock;

    @InjectMocks
    HealthPotion testHealthPotion = HealthPotion.createHealthPotionBySize(PotionSize.REGULAR);

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
        testHealthPotion.use(null);
    }

    @Test
    public void testCollectSuccess() {
        testHealthPotion.use(heroMock);

        verify(heroMock).takeHealing(50);
    }

}
