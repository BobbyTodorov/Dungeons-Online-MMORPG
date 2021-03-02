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
public class ManaPotionTest {

    @Mock
    Hero heroMock;

    @InjectMocks
    ManaPotion testManaPotion = ManaPotion.createManaPotionBySize(PotionSize.REGULAR);

    @Test(expected = IllegalArgumentException.class)
    public void testCreateManaPotionBySizeWithNullArgument() {
        ManaPotion.createManaPotionBySize(null);
    }

    @Test
    public void testCreateManaPotionBySizeSuccess() {
        assertEquals("createManaPotionBySize must return regular size of mana potion",
            50, Objects.requireNonNull(ManaPotion.createManaPotionBySize(PotionSize.REGULAR)).heal());

        assertEquals("createManaPotionBySize must return greater size of mana potion",
            90, Objects.requireNonNull(ManaPotion.createManaPotionBySize(PotionSize.GREATER)).heal());

        assertEquals("createManaPotionBySize must return superior size of mana potion",
            150, Objects.requireNonNull(ManaPotion.createManaPotionBySize(PotionSize.SUPERIOR)).heal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectWithNullArgument() {
        testManaPotion.use(null);
    }

    @Test
    public void testCollectSuccess() {
        testManaPotion.use(heroMock);

        verify(heroMock).takeMana(50);
    }

}
