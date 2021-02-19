package bg.sofia.uni.fmi.mjt.dungeonsonline.server.gameengine;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.PlayerCommand;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Position;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.minion.Minion;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Coordinate;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Map;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.exceptions.OutOfMapBoundsException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.BaseSkill;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public class GameEngine {

    public static final String STEP_ON_HERO_STATUS = "attempt to step on hero ";
    public static final String STEP_ON_TREASURE_STATUS = "attempt to step on treasure";

    private static final String INITIATE_BATTLE_STRING = "BATTLE %s VS %s" + System.lineSeparator();
    private static final String YOU_WON_MESSAGE = "You just won the battle!";
    private static final String YOU_LOST_MESSAGE = "You just lost the battle!";

    private static final String TRADE_MESSAGE = "Traded %s with %s.";
    private static final String HERO_MOVED_MESSAGE = "Hero moved successfully.";
    private static final String TREASURE_COLLECTED_TO_BACKPACK_MESSAGE = "%s collected to backpack.";
    private static final String STEP_ON_OBSTACLE_MESSAGE = "Obstacle there. Hero was not moved.";
    private static final String STEP_ON_BOUND_MESSAGE = "Hero reached bound and was not moved.";
    private static final String INVALID_DIRECTION_MESSAGE = "Invalid direction.";

    private static final int EXPERIENCE_PER_KILLING_HERO = 50;

    private static final StaticObjectsStorage staticObjectsStorage = StaticObjectsStorage.getInstance();
    private static final Map map = Map.getInstance(staticObjectsStorage);

    private static GameEngine instance;

    private GameEngine() {
    }

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }

        return instance;
    }

    /**
     * @return the game map
     */
    public String map() {
        return map.matrix();
    }

    /**
     * Changes random free map field with given hero's symbolToVisualize. Then sets the given hero's position
     * to the position of the changed field.
     *
     * @param hero the hero to be summoned
     */
    public void summonPlayerHero(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        Coordinate summonedHeroPosition =
            map.changeRandomFieldWithGivenSymbolToAnother(Map.FREE_FIELD_SYMBOL, getHeroSymbol(hero));
        hero.setPositionOnMap(summonedHeroPosition);
    }

    /**
     * If the field located on given hero's coordinate is its symbolToVisualize, replace it with a free field symbol.
     *
     * @param hero the hero to be unsummoned
     */
    public void unSummonPlayerHero(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        // if hero's field was not changed before unSummoning, do unSummon
        Coordinate heroCoordinatesOnMap = hero.positionOnMap().coordinate();
        if (map.getFieldSymbol(heroCoordinatesOnMap) == hero.getSymbolToVisualizeOnMap()) {
            map.changeGivenFieldByCoordinatesSymbol(heroCoordinatesOnMap, Map.FREE_FIELD_SYMBOL);
        }
    }

    /**
     * Checks the field that given hero will be moved to.
     * If the field is free, returns status and moves the hero there.
     * If there is a player on the field, returns status followed by that hero's symbolToVisualize. The hero then
     * is NOT moved in order to prevent heroes overlapping.
     * If there is a treasure, returns status and moves the hero there.
     * If there is a minion, initiates a battle with that minion. If hero kills the minion, the hero is moved there.
     *
     * @param hero      the hero to be moved
     * @param direction the direction to be moved in
     * @return result status after moving
     */
    public String moveHero(Hero hero, Direction direction) {
        ArgumentValidator.checkForNullArguments(hero, direction);

        Position currentHeroPosition = hero.positionOnMap();
        Position newHeroPosition = Position.createPosition(currentHeroPosition, direction);

        char newPositionFieldSymbol;
        try {
            newPositionFieldSymbol = map.getFieldSymbol(newHeroPosition.coordinate());
        } catch (OutOfMapBoundsException e) {
            return STEP_ON_BOUND_MESSAGE;
        }

        switch (newPositionFieldSymbol) {
            case Map.FREE_FIELD_SYMBOL -> {
                return moveHeroToPosition(hero, currentHeroPosition, newHeroPosition);
            }
            case Map.OBSTACLE_FIELD_SYMBOL -> {
                return STEP_ON_OBSTACLE_MESSAGE;
            }
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                return STEP_ON_HERO_STATUS + newPositionFieldSymbol;
            }
            case Treasure.SYMBOL_TO_VISUALIZE_ON_MAP -> {
                moveHeroToPosition(hero, currentHeroPosition, newHeroPosition);
                return STEP_ON_TREASURE_STATUS;
            }
            case Minion.SYMBOL_TO_VISUALIZE_ON_MAP -> {
                return moveHeroToMinionField(hero, currentHeroPosition, newHeroPosition);
            }
            default -> {
                return INVALID_DIRECTION_MESSAGE;
            }
        }
    }

    /**
     * @param command  the command to execute
     * @param hero     the hero that executes it
     * @param treasure the treasure to be executed on
     * @return status after dropping or status after consuming
     */
    public String executeCommandOnHeroTreasure(PlayerCommand command, Hero hero, Treasure treasure) {
        ArgumentValidator.checkForNullArguments(command, hero, treasure);

        switch (command) {
            case DROP -> {
                dropTreasureFromHero(hero, treasure);
            }
            case USE -> {
                return treasure.use(hero);
            }
        }

        return null;
    }

    /**
     * A battle is an alternation of attacks between the initiator and the enemy.
     * The initiator goes first. The attacks continue until one of the heroes is dead.
     *
     * @param initiator the hero to initiate the battle
     * @param enemy     the enemy
     * @return information about the battlers followed by win/lose status according to the initiator
     */
    public String battleWithAnotherHero(Hero initiator, Hero enemy) {
        ArgumentValidator.checkForNullArguments(initiator, enemy);

        StringBuilder battleResult = new StringBuilder(String.format(INITIATE_BATTLE_STRING, initiator, enemy));

        while (true) {
            enemy.takeDamage(initiator.attack());
            String resultAfterHit = performKillIfAnyHeroIsDead(initiator, enemy);
            if (resultAfterHit != null) {
                battleResult.append(resultAfterHit);
                break;
            }

            initiator.takeDamage(enemy.attack());
            String resultAfterBeingHit = performKillIfAnyHeroIsDead(initiator, enemy);
            if (resultAfterBeingHit != null) {
                battleResult.append(resultAfterBeingHit);
                break;
            }
        }
        return battleResult.toString();
    }

    /**
     * Removes the treasure (by its index) from initiator's backpack and adds it to otherHero's backpack.
     *
     * @param initiator     the hero that will give a treasure
     * @param otherHero     the hero that will receive the treasure
     * @param treasureIndex the index of the treasure (from initiator's backpack) to be traded
     * @return trade status message
     */
    public String tradeTreasureWithAnotherHero(Hero initiator, Hero otherHero, int treasureIndex) {
        ArgumentValidator.checkForNullArguments(initiator, otherHero);
        ArgumentValidator.checkForNonNegativeArguments(treasureIndex);

        Treasure treasureToTrade = initiator.backpack().remove(treasureIndex);
        otherHero.backpack().addTreasure(treasureToTrade);

        return String.format(TRADE_MESSAGE, treasureToTrade.toString(), otherHero.getName());
    }

    /**
     * Given hero consumes given treasure if possible.
     *
     * @param hero     the hero to consume the treasure
     * @param treasure the treasure to be consumed by hero
     * @return consume status result
     */
    public String heroTryConsumingTreasure(Hero hero, Treasure treasure) {
        String consumeResult = treasure.use(hero);

        if (consumeResult.contains(String.format(BaseSkill.CANT_EQUIP_MESSAGE, "Weapon"))
            || consumeResult.contains(String.format(BaseSkill.CANT_EQUIP_MESSAGE, "Spell"))) {
            return consumeResult + System.lineSeparator() + collectTreasureToHeroBackpack(treasure, hero);
        }

        return consumeResult;
    }

    /**
     * Collects given treasure to given hero's backpack.
     *
     * @return collect status
     */
    public String collectTreasureToHeroBackpack(Treasure treasure, Hero hero) {
        hero.collectTreasure(treasure);

        return String.format(TREASURE_COLLECTED_TO_BACKPACK_MESSAGE, treasure.toString());
    }

    private String moveHeroToPosition(Hero hero, Position oldPosition, Position newPosition) {
        if (map.getFieldSymbol(oldPosition.coordinate()) != Treasure.SYMBOL_TO_VISUALIZE_ON_MAP) { // in case of drop
            map.changeGivenFieldByCoordinatesSymbol(oldPosition.coordinate(), Map.FREE_FIELD_SYMBOL);
        }
        map.changeGivenFieldByCoordinatesSymbol(newPosition.coordinate(), getHeroSymbol(hero));

        hero.setPositionOnMap(newPosition.coordinate());

        return HERO_MOVED_MESSAGE;
    }

    private String moveHeroToMinionField(Hero initiator, Position currentPosition, Position newPosition) {
        Minion enemyMinion = staticObjectsStorage.getMinion();
        String battleString = String.format(INITIATE_BATTLE_STRING, initiator, enemyMinion);

        if (battleWithMinion(initiator, enemyMinion)) {
            moveHeroToPosition(initiator, currentPosition, newPosition);
            return battleString + YOU_WON_MESSAGE;
        } else {
            return battleString;
        }
    }

    private boolean battleWithMinion(Hero initiator, Minion enemy) {
        while (true) {
            enemy.takeDamage(initiator.attack());
            if (!enemy.isAlive()) {
                initiator.gainExperience(enemy.giveExperience());
                map.changeRandomFieldWithGivenSymbolToAnother(Map.FREE_FIELD_SYMBOL, Minion.SYMBOL_TO_VISUALIZE_ON_MAP);
                return true;
            }

            initiator.takeDamage(enemy.attack());
            if (!initiator.isAlive()) {
                return false;
            }
        }
    }

    private String performKillIfAnyHeroIsDead(Hero hero1, Hero hero2) {
        if (!hero1.isAlive()) {
            heroKillsAnotherHero(hero2, hero1);

            return YOU_LOST_MESSAGE;

        } else if (!hero2.isAlive()) {
            heroKillsAnotherHero(hero1, hero2);

            return YOU_WON_MESSAGE;
        }

        return null;
    }

    private void heroKillsAnotherHero(Hero killer, Hero deadMan) {
        killer.gainExperience(EXPERIENCE_PER_KILLING_HERO);

        if (deadMan.backpack().size() == 0) {
            map.changeGivenFieldByCoordinatesSymbol(deadMan.positionOnMap().coordinate(), Map.FREE_FIELD_SYMBOL);
        } else {
            dropTreasureFromHero(deadMan, deadMan.backpack().remove(0));
        }
    }

    private char getHeroSymbol(Hero hero) {
        return hero.getSymbolToVisualizeOnMap();
    }

    private void dropTreasureFromHero(Hero hero, Treasure treasure) {
        staticObjectsStorage.addTreasure(treasure);
        map.changeGivenFieldByCoordinatesSymbol(hero.positionOnMap().coordinate(), Treasure.SYMBOL_TO_VISUALIZE_ON_MAP);
    }
}
