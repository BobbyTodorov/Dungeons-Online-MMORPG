package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
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
    Hero testHero;

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
        testSpell.consume(null);
    }

    @Test
    public void testCollectWithLowerHeroLevel() {
        when(testHero.getLevel()).thenReturn(1);

        assertEquals("collect with lower hero level must not call learn to spell",
            "Spell level is too high for you to equip.", testSpell.consume(testHero));

        verify(testHero, times(0)).learn(testSpell);
    }

    @Test
    public void testCollectWithHigherHeroLevelSuccess() {
        when(testHero.getLevel()).thenReturn(3);

        assertEquals("collect with higher hero level must call learn to spell",
            "Learned Spell  Level: 2 Damage points: 2, Mana cost: 1", testSpell.consume(testHero));

        verify(testHero).learn(testSpell);
    }
}
