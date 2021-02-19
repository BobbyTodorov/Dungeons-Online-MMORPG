package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Spell;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Weapon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class HeroTest {

    Weapon testWeapon = new Weapon("", 1, 1);
    Stats testStats = new Stats(1, 1, 1, 1);

    @Test(expected = IllegalArgumentException.class)
    public void testCollectTreasureWithNullArgument() {
        new Hero("", testStats).collectTreasure(null);
    }

    @Test
    public void testCollectTreasureSuccess() {
        Hero testHero = new Hero("", testStats);
        testHero.collectTreasure(testWeapon);


        assertEquals("collectTreasure must add given treasure to hero's backpack",
            List.of(testWeapon), testHero.backpack().getTreasures());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGainExperienceWithNegativeArgument() {
        new Hero("", testStats).gainExperience(-1);
    }

    @Test
    public void testGainExperienceNeededToLevelUpSuccess() {
        assertEquals("gainExperience must gain the experience to hero, level up the hero if the amount of " +
                "experience is enough and return the experience left after leveling up",
            5, new Hero("", testStats).gainExperience(25));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTakeHealingWithNegativeHealingPoints() {
        new Hero("", testStats).takeHealing(-1);
    }

    @Test
    public void testTakeHealingOnDeadHero() {
        testStats.decreaseCurrentHealth(1);
        Hero testHero = new Hero("", testStats);

        testHero.takeHealing(1);

        assertFalse("hero must take no healing if it is already dead", testHero.isAlive());
    }

    @Test
    public void testTakeHealingSuccess() {
        Stats testStats = new Stats(10, 1, 1, 1);
        testStats.decreaseCurrentHealth(5);
        Hero testHero = new Hero("", testStats);

        testHero.takeHealing(1);

        assertEquals("takeHealing should increase hero's health correctly",
            6, testHero.getStats().getCurrentHealth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTakeManaWithNegativeManaPoints() {
        new Hero("", testStats).takeMana(-1);
    }

    @Test
    public void testTakeManaSuccess() {
        Stats testStats = new Stats(1, 10, 1, 1);
        testStats.decreaseCurrentMana(5);

        Hero testHero = new Hero("", testStats);
        testHero.takeMana(1);

        assertEquals("takeMana should increase hero's mana correctly",
            6, testHero.getStats().getCurrentMana());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEquipWithNullArgument() {
        new Hero("", testStats).equip(null);
    }

    @Test
    public void testEquipWithTooHighLevelWeapon() {
        Hero testHero = new Hero("", testStats);
        testHero.equip(new Weapon("", 1, 2));

        assertNull("equip must not equip weapon which level is higher than hero's level", testHero.getWeapon());
    }

    @Test
    public void testEquipWithSameOrLowerLevelWeaponThanHeroLevelWhenHeroHasNoWeaponEquipped() {
        Weapon testWeapon = new Weapon("", 1, 1);

        Hero testHero = new Hero("", testStats);
        testHero.equip(testWeapon);

        assertEquals("equip must equip weapon which level is lower or equal to hero's level " +
            "when hero has no equipped weapon", testWeapon, testHero.getWeapon());
    }

    @Test
    public void testEquipWithSameOrLowerLevelWeaponThanHeroLevelWhenHeroHasWeaponWithHigherDamageEquipped() {
        Weapon testWeapon = new Weapon("", 2, 1);

        Hero testHero = new Hero("", testStats);
        testHero.equip(testWeapon);
        testHero.equip(new Weapon("", 1, 1));

        assertEquals("equip must not equip weapon which level is lower or equal to hero's level " +
            "when hero has equipped weapon with higher damage", testWeapon, testHero.getWeapon());
    }

    @Test
    public void testEquipWithSameOrLowerLevelWeaponThanHeroLevelWhenHeroHasWeaponWithLowerDamageEquipped() {
        Weapon testWeapon = new Weapon("", 2, 1);

        Hero testHero = new Hero("", testStats);
        testHero.equip(new Weapon("", 1, 1));
        testHero.equip(testWeapon);

        assertEquals("equip must equip weapon which level is lower or equal to hero's level " +
            "when hero has equipped weapon with lower damage", testWeapon, testHero.getWeapon());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLearnWithNullArgument() {
        new Hero("", testStats).learn(null);
    }

    @Test
    public void testLearnWithTooHighLevelSpell() {
        Hero testHero = new Hero("", testStats);
        testHero.learn(new Spell("", 1, 2, 1));

        assertNull("learn must not learn spell which level is higher than hero's level", testHero.getSpell());
    }

    @Test
    public void testLearnWithSameOrLowerLevelSpellThanHeroLevelWhenHeroHasNoSpellEquipped() {
        Spell testSpell = new Spell("", 1, 1, 1);

        Hero testHero = new Hero("", testStats);
        testHero.learn(testSpell);

        assertEquals("learn must learn spell which level is lower or equal to hero's level " +
            "when hero has no learnt spell", testSpell, testHero.getSpell());
    }

    @Test
    public void testLearnWithSameOrLowerLevelSpellThanHeroLevelWhenHeroHasSpellWithHigherDamageLearnt() {
        Spell testSpell = new Spell("", 2, 1, 1);

        Hero testHero = new Hero("", testStats);
        testHero.learn(testSpell);
        testHero.learn(new Spell("", 1, 1, 1));

        assertEquals("learn must not learn spell which level is lower or equal to hero's level " +
            "when hero has learnt spell with higher damage", testSpell, testHero.getSpell());
    }

    @Test
    public void testLearnWithSameOrLowerLevelSpellThanHeroLevelWhenHeroHasSpellWithLowerDamageLearnt() {
        Spell testSpell = new Spell("", 2, 1, 1);

        Hero testHero = new Hero("", testStats);
        testHero.learn(new Spell("", 1, 1, 1));
        testHero.learn(testSpell);

        assertEquals("learn must learn spell which level is lower or equal to hero's level " +
            "when hero has learnt spell with lower damage", testSpell, testHero.getSpell());
    }
}
