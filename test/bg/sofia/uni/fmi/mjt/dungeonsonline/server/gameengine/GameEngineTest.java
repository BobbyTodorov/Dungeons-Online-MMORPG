package bg.sofia.uni.fmi.mjt.dungeonsonline.server.gameengine;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.playercommands.PlayerCommand;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Spell;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class GameEngineTest {


    private final static Stats testStats = new Stats(1, 1, 1, 1);
    private final static Hero testHero = new Hero("", testStats);
    private final static Spell testTreasure = new Spell("", 1, 1, 1);

    GameEngine testGameEngine = GameEngine.getInstance();

    @Test(expected = IllegalArgumentException.class)
    public void testMoveHeroWithNullHeroArgument() {
        testGameEngine.moveHero(null, Direction.LEFT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveHeroWithNullDirectionArgument() {
        testGameEngine.moveHero(testHero, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteCommandOnHeroTreasureWithNullCommand() {
        testGameEngine.executeCommandOnHeroTreasure(null, testHero, testTreasure);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteCommandOnHeroTreasureWithNullHero() {
        testGameEngine.executeCommandOnHeroTreasure(PlayerCommand.DROP, null, testTreasure);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteCommandOnHeroTreasureWithNullTreasure() {
        testGameEngine.executeCommandOnHeroTreasure(PlayerCommand.DROP, testHero, null);
    }

    @Test
    public void testExecuteCommandOnHeroTreasureWithInvalidCommand() {
        String actual = testGameEngine.executeCommandOnHeroTreasure(PlayerCommand.ATTACK, testHero, testTreasure);

        assertNull("executeCommandOnHeroTreasure with invalid PlayerCommand must return null", actual);
    }

    @Test
    public void testExecuteCommandOnHeroTreasureWithCommandUse() {
        String actual = testGameEngine.executeCommandOnHeroTreasure(PlayerCommand.USE, testHero, testTreasure);
        String expected = "Learned Spell  Level: 1 Damage points: 1, Mana cost: 1";
        assertEquals("executeCommandOnHeroTreasure with PlayerCommand USE " +
            "must return result from performing collect on treasure by hero", expected, actual);
    }

    @Test
    public void testExecuteCommandOnHeroTreasureWithCommandDrop() {
        String actual = testGameEngine.executeCommandOnHeroTreasure(PlayerCommand.DROP, testHero, testTreasure);
        assertNull("executeCommandOnHeroTreasure with PlayerCommand DROP " +
            "must return null", actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBattleWithPlayerWithNullInitiatorArgument() {
        testGameEngine.battleWithAnotherHero(null, testHero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBattleWithPlayerWithNullEnemyArgument() {
        testGameEngine.battleWithAnotherHero(testHero, null);
    }

    @Test
    public void testBattleWithPlayerWithWeakerEnemy() {
        Hero testStrongerInitiator = new Hero("", new Stats(1000, 1000, 1000, 1000));
        Hero testWeakerEnemy = new Hero("", new Stats(1, 1, 1, 1));

        String actual = testGameEngine.battleWithAnotherHero(testStrongerInitiator, testWeakerEnemy);
        String expected = "BATTLE Hero 0 {name=, level=1, stats=health=1000/1000, mana=1000/1000, attackPoints=1000, " +
            "defensePoints=1000}, weapon=null, spell=null, experience=0} VS Hero 0 {name=, level=1, stats=health=1/1, " +
            "mana=1/1, attackPoints=1, defensePoints=1}, weapon=null, spell=null, experience=0}" +
            System.lineSeparator() + "You just won the battle!";

        assertEquals("battleWithPlayer with weaker enemy should return battle string followed by winning " +
            "first player string", expected, actual);
    }

    @Test
    public void testBattleWithPlayerWithStrongerEnemy() {
        Hero testWeakerInitiator = new Hero("", new Stats(1, 1, 1, 1));
        Hero testStrongerEnemy = new Hero("", new Stats(1000, 1000, 1000, 1000));

        String actual = testGameEngine.battleWithAnotherHero(testWeakerInitiator, testStrongerEnemy);
        String expected = "BATTLE Hero 0 {name=, level=1, stats=health=1/1, mana=1/1, attackPoints=1, " +
            "defensePoints=1}, weapon=null, spell=null, experience=0} VS Hero 0 {name=, level=1, " +
            "stats=health=1000/1000, mana=1000/1000, attackPoints=1000, defensePoints=1000}, " +
            "weapon=null, spell=null, experience=0}" + System.lineSeparator() + "You just lost the battle!";

        assertEquals("battleWithPlayer with stronger enemy should return battle string only",
            expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTradeWithPlayerWithNullInitiatorArgument() {
        testGameEngine.tradeTreasureWithAnotherHero(null, testHero, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTradeWithPlayerWithNullOtherHeroArgument() {
        testGameEngine.tradeTreasureWithAnotherHero(testHero, null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTradeWithPlayerWithNegativeTreasureIndexArgument() {
        testGameEngine.tradeTreasureWithAnotherHero(null, testHero, -1);
    }

    @Test
    public void testTradeWithPlayerSuccess() {
        Hero testInitiator = new Hero("", testStats);
        Hero otherHero = new Hero("", testStats);
        testInitiator.collectTreasure(testTreasure);

        String actual = testGameEngine.tradeTreasureWithAnotherHero(testInitiator, otherHero, 0);

        assertEquals("tradeWithPlayer should remove treasure from initiator's backpack and add it to otherHero's",
            "Traded Spell{name='', damage=1, level=1, MANA_COST=1} with .", actual);
    }
}
