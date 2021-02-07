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
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public class GameEngine {

    public static final String STEP_ON_PLAYER_STATUS = "player %s";
    public static final String STEP_ON_TREASURE_STATUS = "treasure";

    private static final String INITIATE_BATTLE_STRING = "BATTLE %s VS %s" + System.lineSeparator();
    private static final String BATTLE_WIN_MESSAGE = System.lineSeparator() + "%s HAS WON!";

    private static final String TRADE_MESSAGE = "Traded %s with %s.";
    private static final String HERO_MOVED_MESSAGE = "Hero moved successfully.";
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

        map.changeRandomFieldWithGivenSymbolToAnother(getHeroSymbol(hero), Map.FREE_FIELD_SYMBOL);
    }

    public String moveHero(Hero hero, Direction direction) {
        ArgumentValidator.checkForNullArguments(hero, direction);

        Position currentHeroPosition = hero.positionOnMap();
        Position newHeroPosition = Position.createPosition(currentHeroPosition, direction);

        char newPositionFieldSymbol;
        try {
            newPositionFieldSymbol = map.getFieldSymbol(newHeroPosition.getCoordinate());
        } catch (OutOfMapBoundsException e) {
            return STEP_ON_BOUND_MESSAGE;
        }

        switch (newPositionFieldSymbol) {
            case Map.FREE_FIELD_SYMBOL -> { return moveHeroToPosition(hero, currentHeroPosition, newHeroPosition); }
            case Map.OBSTACLE_FIELD_SYMBOL -> { return STEP_ON_OBSTACLE_MESSAGE; }
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                return String.format(STEP_ON_PLAYER_STATUS, newPositionFieldSymbol);
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
            case USE -> { return treasure.collect(hero); }
        }

        return null;
    }

    public String battleWithPlayer(Hero initiator, Hero enemy) {
        ArgumentValidator.checkForNullArguments(initiator, enemy);

        StringBuilder battleResult = new StringBuilder(String.format(INITIATE_BATTLE_STRING, initiator, enemy));

        while(true){
            enemy.takeDamage(initiator.attack());
            String resultAfterHit = checkIfHeroHasKilledAnotherHero(initiator, enemy);
            if (resultAfterHit != null) {
                battleResult.append(resultAfterHit).append(DEAD_PLAYER_MESSAGE);
                break;
            }

            initiator.takeDamage(enemy.attack());
            String resultAfterBeingHit = checkIfHeroHasKilledAnotherHero(enemy, initiator);
            if (resultAfterBeingHit != null) {
                battleResult.append(resultAfterBeingHit);
                break;
            }
        }
        return battleResult.toString();
    }

    public String tradeWithPlayer(Hero initiator, Hero otherHero, int treasureIndex) {
        ArgumentValidator.checkForNullArguments(initiator, otherHero);
        ArgumentValidator.checkForNonNegativeArguments(treasureIndex);

        Treasure treasureToTrade = initiator.backpack().remove(treasureIndex);
        otherHero.backpack().addTreasure(treasureToTrade);

        return String.format(TRADE_MESSAGE, treasureToTrade.toString(), otherHero.getName());
    }

    private String moveHeroToPosition(Hero hero, Position oldPosition, Position newPosition) {
        if (map.getFieldSymbol(oldPosition.getCoordinate()) != Treasure.SYMBOL_TO_VISUALIZE_ON_MAP) { // in case of drop
            map.changeGivenFieldByCoordinatesSymbol(oldPosition.getCoordinate(), Map.FREE_FIELD_SYMBOL);
        }
        map.changeGivenFieldByCoordinatesSymbol(newPosition.getCoordinate(), getHeroSymbol(hero));

        hero.setPositionOnMap(newPosition.getCoordinate());

        return HERO_MOVED_MESSAGE;
    }

    private String moveHeroToMinionField(Hero initiator, Position currentPosition, Position newPosition) {
        Minion enemyMinion = staticObjectsStorage.getMinion();
        String battleString = String.format(INITIATE_BATTLE_STRING, initiator, enemyMinion);

        if (battleWithMinion(initiator, enemyMinion)) {
            moveHeroToPosition(initiator, currentPosition, newPosition);
            return battleString + String.format(BATTLE_WIN_MESSAGE, initiator.getName());
        } else {
            return battleString + String.format(BATTLE_WIN_MESSAGE, enemyMinion.getName());
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

    private String checkIfHeroHasKilledAnotherHero(Hero hero1, Hero hero2) {
        if(!hero2.isAlive()) {
            hero1.gainExperience(EXPERIENCE_PER_KILLING_HERO);

            if (hero2.backpack().size() == 0) {
                map.changeGivenFieldByCoordinatesSymbol(hero2.positionOnMap().getCoordinate(), Map.FREE_FIELD_SYMBOL);
            } else {
                dropTreasureFromHero(hero2, hero2.backpack().remove(0));
            }

            return String.format(BATTLE_WIN_MESSAGE, hero1.getName());
        }

        return null;
    }

    private char getHeroSymbol(Hero hero) { return hero.getSymbolToVisualizeOnMap(); }

    private void dropTreasureFromHero(Hero hero, Treasure treasure) {
        staticObjectsStorage.addTreasure(treasure);
        map.changeGivenFieldByCoordinatesSymbol(hero.positionOnMap().getCoordinate(), Treasure.SYMBOL_TO_VISUALIZE_ON_MAP);
    }
}
