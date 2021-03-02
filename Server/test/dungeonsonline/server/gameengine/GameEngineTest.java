package dungeonsonline.server.gameengine;

import dungeonsonline.server.actor.attributes.Stats;
import dungeonsonline.server.actor.hero.Hero;
import dungeonsonline.server.actor.hero.movement.Direction;
import dungeonsonline.server.actor.minion.Minion;
import dungeonsonline.server.actor.minion.MinionDifficultyLevel;
import dungeonsonline.server.map.Coordinate;
import dungeonsonline.server.map.Map;
import dungeonsonline.server.storage.StaticObjectsStorage;
import dungeonsonline.server.treasure.skill.Spell;
import dungeonsonline.server.treasure.skill.Weapon;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GameEngineTest {


    private final static Stats testStats = new Stats(1, 1, 1, 1);
    private final static Hero testHero = new Hero("", testStats);
    private final static Spell testTreasure = new Spell("", 1, 1, 1);

    @Mock
    StaticObjectsStorage staticObjectsStorageMock;

    @Mock
    Map mapMock;

    @InjectMocks
    private GameEngine testGameEngine;

    @Test(expected = IllegalArgumentException.class)
    public void testSummonPlayerHeroWithNullArgument() {
        testGameEngine.summonPlayerHero(null);
    }

    @Test
    public void testSummonPlayerHeroSuccess() {
        testGameEngine.summonPlayerHero(testHero);
        Coordinate heroCoord = verify(mapMock).changeRandomFieldWithGivenSymbolToAnother('.', testHero.getSymbolToVisualizeOnMap());

        Assert.assertEquals("summonPlayerHero must summon the hero on a free field of the map successfully",
            heroCoord, testHero.positionOnMap().coordinate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnSummonPlayerHeroWithNullArgument() {
        testGameEngine.unSummonPlayerHero(null);
    }

    @Test
    public void testUnSummonPlayerHeroSuccess() {
        Coordinate heroCoord = testHero.positionOnMap().coordinate();
        when(mapMock.getFieldSymbol(heroCoord)).thenReturn(testHero.getSymbolToVisualizeOnMap());

        testGameEngine.unSummonPlayerHero(testHero);
        verify(mapMock).changeGivenFieldByCoordinatesSymbol(heroCoord, '.');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveHeroWithNullHeroArgument() {
        testGameEngine.moveHero(null, Direction.LEFT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveHeroWithNullDirectionArgument() {
        testGameEngine.moveHero(testHero, null);
    }

    @Test
    public void testMoveHeroToFreeField() {
        when(mapMock.getFieldSymbol(any())).thenReturn('.');

        assertEquals("Moving a hero to free field should return correct message",
            "Hero moved successfully.", testGameEngine.moveHero(testHero, Direction.LEFT));
    }

    @Test
    public void testMoveHeroToObstacleField() {
        when(mapMock.getFieldSymbol(any())).thenReturn('#');

        assertEquals("Moving a hero to obstacle field should return correct message",
            "Obstacle there. Hero was not moved.", testGameEngine.moveHero(testHero, Direction.LEFT));
    }

    @Test
    public void testMoveHeroToFieldWithAnotherHero() {
        Hero testHeroToAnotherHero = new Hero("", new Stats(1, 1, 1, 1));
        when(mapMock.getFieldSymbol(any())).thenReturn('2');

        assertEquals("Moving a hero to field with another hero should return correct status message",
            "attempt to step on hero 2", testGameEngine.moveHero(testHeroToAnotherHero, Direction.LEFT));
    }

    @Test
    public void testMoveHeroToTreasureField() {
        when(mapMock.getFieldSymbol(any())).thenReturn('T');

        assertEquals("Moving a hero to treasure field should return correct status message",
            "attempt to step on treasure", testGameEngine.moveHero(testHero, Direction.LEFT));
    }

    @Test
    public void testMoveHeroToMinionField() {
        Hero testBattleHero = new Hero("", new Stats(1, 1, 1, 1));
        when(mapMock.getFieldSymbol(any())).thenReturn('M');
        when(staticObjectsStorageMock.getMinion())
            .thenReturn(Minion.createMinionByDifficultyLevel(MinionDifficultyLevel.EASY));

        String expected = "BATTLE Hero 0 {name=, level=1, stats=health=1/1, mana=1/1, attackPoints=1, " +
            "defensePoints=1}, weapon=null, spell=null, experience=0} VS Minion{name='easy minion', level=1, " +
            "stats=health=50/50, mana=60/60, attackPoints=15, defensePoints=15}, weapon=null, " +
            "spell=Spell{name='easy spell', damage=15, level=1, MANA_COST=10}}" + System.lineSeparator();

        assertEquals("Moving a hero to minion field should return correct message",
            expected, testGameEngine.moveHero(testBattleHero, Direction.LEFT));
    }

    @Test
    public void testMoveHeroToInvalidField() {
        when(mapMock.getFieldSymbol(any())).thenReturn('%');

        assertEquals("Moving a hero to an invalid field should return correct message",
            "Attempting to move to an invalid field.", testGameEngine.moveHero(testHero, Direction.LEFT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectTreasureToHeroBackpackWithNullTreasureArgument() {
        testGameEngine.collectTreasureToHeroBackpack(null, testHero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectTreasureToHeroBackpackWithNullHeroArgument() {
        testGameEngine.collectTreasureToHeroBackpack(testTreasure, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeroTryUsingTreasureWithNullTreasureArgument() {
        testGameEngine.heroTryUsingTreasure(testHero, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeroTryUsingTreasureWithNullHeroArgument() {
        testGameEngine.heroTryUsingTreasure(null, testTreasure);
    }

    @Test
    public void testHeroTryUsingTreasureSuccess() {
        String actual = testGameEngine.heroTryUsingTreasure(testHero, testTreasure);
        String expected = "Learned Spell  Level: 1 Damage points: 1, Mana cost: 1";
        assertEquals("heroTryUsingTreasure with success must return correct message", expected, actual);
    }

    @Test
    public void testHeroTryUsingTreasureSuccessWithWeaponThatCantBeEquipped() {
        String actual = testGameEngine.heroTryUsingTreasure(testHero, new Weapon("", 1, 10));
        String expected = "Weapon level is too high for you to use." + System.lineSeparator() +
            "name='', damage=1, level=10 collected to backpack.";
        assertEquals("tryUsingTreasure with weapon that can't be equipped must return correct message" +
                "and collect the weapon to hero's backpack", expected, actual);
    }

    @Test
    public void testHeroTryUsingTreasureSuccessWithSpellThatCantBeLearnt() {
        String actual = testGameEngine.heroTryUsingTreasure(testHero, new Spell("", 1, 10, 1));
        String expected = "Spell level is too high for you to use." + System.lineSeparator() +
            "Spell{name='', damage=1, level=10, MANA_COST=1} collected to backpack.";
        assertEquals("tryUsingTreasure with spell that can't be learnt must return correct message" +
            "and collect the spell to hero's backpack", expected, actual);
    }

    @Test
    public void testCollectTreasureToHeroBackpackSuccess() {
        String actual = testGameEngine.collectTreasureToHeroBackpack(testTreasure, testHero);
        String expected = "Spell{name='', damage=1, level=1, MANA_COST=1} collected to backpack.";
        assertEquals("collectTreasureToHeroBackpack must return correct message", expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDropTreasureFromHeroWithNullHeroArgument() {
        testGameEngine.dropTreasureFromHero(null, testTreasure);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDropTreasureFromHeroWithNullTreasureArgument() {
        testGameEngine.collectTreasureToHeroBackpack(null, testHero);
    }

    @Test
    public void testDropTreasureFromHeroSuccess() {
        String actual = testGameEngine.dropTreasureFromHero(testHero, testTreasure);
        String expected = "Spell{name='', damage=1, level=1, MANA_COST=1} was dropped on your position successfully.";
        assertEquals("dropTreasureFromHero must return correct message", expected, actual);
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
