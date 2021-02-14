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

    private static final String TRADE_MESSAGE = "Traded %s with %s.";
    private static final String HERO_MOVED_MESSAGE = "Hero moved successfully.";
    private static final String TREASURE_COLLECTED_TO_BACKPACK_MESSAGE = "%s collected to backpack.";
    private static final String STEP_ON_OBSTACLE_MESSAGE = "Obstacle there. Hero was not moved.";
    private static final String STEP_ON_BOUND_MESSAGE = "Hero reached bound and was not moved.";
    private static final String INVALID_DIRECTION_MESSAGE = "Invalid direction.";
    private static final String DEAD_PLAYER_MESSAGE = "You just died.";

    private static final int EXPERIENCE_PER_KILLING_HERO = 50;

    private static final StaticObjectsStorage staticObjectsStorage = StaticObjectsStorage.getInstance();
    private static final Map map = Map.getInstance(staticObjectsStorage);

    private static GameEngine instance;

    private GameEngine() {}

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }

        return instance;
    }

    public String getMapToVisualize() {
        return map.getMap();
    }

    public void summonPlayerHero(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        Coordinate summonedHeroPosition = map.changeRandomFieldWithGivenSymbolToAnother(Map.FREE_FIELD_SYMBOL, getHeroSymbol(hero));
        hero.setPositionOnMap(summonedHeroPosition);
    }

    public void unSummonPlayerHero(Hero hero) {
        ArgumentValidator.checkForNullArguments(hero);

        // if hero's field was not changed before unSummoning, do unSummon
        Coordinate heroCoordinatesOnMap = hero.positionOnMap().coordinate();
        if (map.getFieldSymbol(heroCoordinatesOnMap) == hero.getSymbolToVisualizeOnMap()) {
            map.changeGivenFieldByCoordinatesSymbol(heroCoordinatesOnMap, Map.FREE_FIELD_SYMBOL);
        }
    }

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
            case Map.FREE_FIELD_SYMBOL -> { return moveHeroToPosition(hero, currentHeroPosition, newHeroPosition); }
            case Map.OBSTACLE_FIELD_SYMBOL -> { return STEP_ON_OBSTACLE_MESSAGE; }
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
            default -> { return INVALID_DIRECTION_MESSAGE; }
        }
    }

    public String executeCommandOnHeroTreasure(PlayerCommand command, Hero hero, Treasure treasure) {
        ArgumentValidator.checkForNullArguments(command, hero, treasure);

        switch (command) {
            case DROP -> { dropTreasureFromHero(hero, treasure); }
            case USE -> { return treasure.consume(hero); }
        }

        return null;
    }

    public String battleWithAnotherHero(Hero initiator, Hero enemy) {
        ArgumentValidator.checkForNullArguments(initiator, enemy);

        StringBuilder battleResult = new StringBuilder(String.format(INITIATE_BATTLE_STRING, initiator, enemy));

        while(true){
            enemy.takeDamage(initiator.attack());
            String resultAfterHit = performKillIfEnemyIsNotAlive(initiator, enemy);
            if (resultAfterHit != null) {
                battleResult.append(resultAfterHit);
                break;
            }

            initiator.takeDamage(enemy.attack());
            String resultAfterBeingHit = performKillIfEnemyIsNotAlive(enemy, initiator);
            if (resultAfterBeingHit != null) {
                battleResult.append(resultAfterBeingHit);
                break;
            }
        }
        return battleResult.toString();
    }

    public String tradeTreasureWithAnotherHero(Hero initiator, Hero otherHero, int treasureIndex) {
        ArgumentValidator.checkForNullArguments(initiator, otherHero);
        ArgumentValidator.checkForNonNegativeArguments(treasureIndex);

        Treasure treasureToTrade = initiator.backpack().remove(treasureIndex);
        otherHero.backpack().addTreasure(treasureToTrade);

        return String.format(TRADE_MESSAGE, treasureToTrade.toString(), otherHero.getName());
    }

    public String heroTryConsumingTreasure(Hero hero, Treasure treasure) {
        String consumeResult = treasure.consume(hero);

        if (consumeResult.equals(BaseSkill.CANT_EQUIP_MESSAGE)) {
            return consumeResult + System.lineSeparator() + collectTreasureToHeroBackpack(treasure, hero);
        }

        return consumeResult;
    }

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
            return battleString + DEAD_PLAYER_MESSAGE;
        }
    }

    private boolean battleWithMinion(Hero initiator, Minion enemy) {
        while (true) {
            enemy.takeDamage(initiator.attack());
            if(!enemy.isAlive()) {
                initiator.gainExperience(enemy.giveExperience());
                map.changeRandomFieldWithGivenSymbolToAnother(Map.FREE_FIELD_SYMBOL, Minion.SYMBOL_TO_VISUALIZE_ON_MAP);
                return true;
            }

            initiator.takeDamage(enemy.attack());
            if(!initiator.isAlive()) {
                return false;
            }
        }
    }

    private String performKillIfEnemyIsNotAlive(Hero hero1, Hero hero2) {
        if(!hero2.isAlive()) {
            hero1.gainExperience(EXPERIENCE_PER_KILLING_HERO);

            if (hero2.backpack().size() == 0) {
                map.changeGivenFieldByCoordinatesSymbol(hero2.positionOnMap().coordinate(), Map.FREE_FIELD_SYMBOL);
            } else {
                dropTreasureFromHero(hero2, hero2.backpack().remove(0));
            }

            return YOU_WON_MESSAGE;
        }

        return null;
    }

    private char getHeroSymbol(Hero hero) { return hero.getSymbolToVisualizeOnMap(); }

    private void dropTreasureFromHero(Hero hero, Treasure treasure) {
        staticObjectsStorage.addTreasure(treasure);
        map.changeGivenFieldByCoordinatesSymbol(hero.positionOnMap().coordinate(), Treasure.SYMBOL_TO_VISUALIZE_ON_MAP);
    }
}
