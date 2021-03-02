package dungeonsonline.server.treasure.skill;

import dungeonsonline.server.actor.attributes.Stats;
import dungeonsonline.server.actor.hero.Hero;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WeaponTest {

    @Mock
    Hero heroMock = new Hero("", new Stats(1, 1, 1, 1));

    @InjectMocks
    Weapon testWeapon = new Weapon("", 2, 2);

    @Test(expected = IllegalArgumentException.class)
    public void testCollectWithNullArgument() {
        testWeapon.use(null);
    }

    @Test
    public void testCollectWithLowerHeroLevel() {
        when(heroMock.getLevel()).thenReturn(1);

        assertEquals("collect with lower hero level must not call equip to weapon",
            "Weapon level is too high for you to use.", testWeapon.use(heroMock));

        verify(heroMock, times(0)).equip(testWeapon);
    }

    @Test
    public void testCollectWithHigherHeroLevelSuccess() {
        when(heroMock.getLevel()).thenReturn(3);

        assertEquals("collect with higher hero level must call equip to weapon",
            "Equipped Weapon  Level: 2 Damage points: 2", testWeapon.use(heroMock));

        verify(heroMock).equip(testWeapon);
    }

}
