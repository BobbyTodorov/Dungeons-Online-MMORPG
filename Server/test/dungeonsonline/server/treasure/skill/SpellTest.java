package dungeonsonline.server.treasure.skill;

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
public class SpellTest {

    @Mock
    Hero heroMock;

    @InjectMocks
    Spell testSpell = new Spell("", 2, 2, 1);

    @Test(expected = IllegalArgumentException.class)
    public void testConstructingSpellWithNullName() {
        new Spell(null, 1, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructingSpellWithNegativeDamage() {
        new Spell("", -1, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructingSpellWithNegativeLevel() {
        new Spell("", 1, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructingSpellWithNegativeManaCost() {
        new Spell("", 1, 1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectWithNullArgument() {
        testSpell.use(null);
    }

    @Test
    public void testCollectWithLowerHeroLevel() {
        when(heroMock.getLevel()).thenReturn(1);

        assertEquals("collect with lower hero level must not call learn to spell",
            "Spell level is too high for you to use.", testSpell.use(heroMock));

        verify(heroMock, times(0)).learn(testSpell);
    }

    @Test
    public void testCollectWithHigherHeroLevelSuccess() {
        when(heroMock.getLevel()).thenReturn(3);

        assertEquals("collect with higher hero level must call learn to spell",
            "Learned Spell  Level: 2 Damage points: 2, Mana cost: 1", testSpell.use(heroMock));

        verify(heroMock).learn(testSpell);
    }
}
