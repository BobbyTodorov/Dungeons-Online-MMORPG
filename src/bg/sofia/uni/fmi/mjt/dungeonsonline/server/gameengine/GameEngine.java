package bg.sofia.uni.fmi.mjt.dungeonsonline.server.gameengine;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Backpack;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Position;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.minion.Minion;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Coordinate;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Map;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.PlayersConnectionStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;

import java.net.Socket;
import java.nio.channels.SocketChannel;

public class GameEngine {

    private static int maxNumberOfPlayers;

    private static final StaticObjectsStorage staticObjectsStorage = StaticObjectsStorage.getInstance();
    private static final Map map = Map.getInstance(staticObjectsStorage);
    private static final PlayersConnectionStorage playersConnectionStorage = PlayersConnectionStorage.getInstance(maxNumberOfPlayers);

    private static GameEngine instance;

    private GameEngine() {}

    public static GameEngine getInstance(int maxNumberOfPlayers) {
        if (instance == null) {
            instance = new GameEngine();
            GameEngine.maxNumberOfPlayers = maxNumberOfPlayers;
        }

        return instance;
    }

    public String getMapToVisualize() {
        assert map != null;
        return map.getMap();
    }

    public void summonPlayerHero(Hero hero) {
        assert map != null;
        Coordinate summonedHeroPosition = map.changeRandomFieldWithGivenSymbolToAnother(Map.FREE_FIELD_SYMBOL, getHeroSymbol(hero));
        hero.setPositionOnMap(summonedHeroPosition);
    }

    public void unSummonPlayerHero(Hero hero) {
        map.changeRandomFieldWithGivenSymbolToAnother(getHeroSymbol(hero), Map.FREE_FIELD_SYMBOL);
    }

    public String moveHero(Hero hero, Direction direction) {
        Position currentHeroPosition = hero.getPositionOnMap();
        Position newHeroPosition = Position.createPosition(currentHeroPosition, direction);

        char newPositionFieldSymbol;
        try {
            newPositionFieldSymbol = map.getFieldSymbol(newHeroPosition.getCoordinate());
        } catch (ArrayIndexOutOfBoundsException e) { //TODO proper map exception
            return "Hero reached a bound and was not moved.";
        }

        switch (newPositionFieldSymbol) {
            case Map.FREE_FIELD_SYMBOL -> { return moveHeroToPosition(hero, currentHeroPosition, newHeroPosition); }
            case Map.OBSTACLE_FIELD_SYMBOL -> { return "Obstacle there. Hero was not moved."; }
            case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                //moveHeroToPosition(hero, currentHeroPosition, newHeroPosition); //TODO ask lector
                return "player " + newPositionFieldSymbol;
            }
            case Treasure.SYMBOL_TO_VISUALIZE_ON_MAP -> {
                moveHeroToPosition(hero, currentHeroPosition, newHeroPosition);
                return "treasure";
            }
            case Minion.SYMBOL_TO_VISUALIZE_ON_MAP -> {
                return moveHeroToMinionField(hero, currentHeroPosition, newHeroPosition);
            }
            default -> { return "invalid direction"; }
        }
    }

    public String addTreasureToBackpack(SocketChannel client, Treasure item) {
        playersConnectionStorage.getPlayerHero(client).collectTreasure(item);

        return item.toString() + " collected to backpack.";
    }

    public String consumeTreasure(SocketChannel client, Treasure item) {
        item.collect(playersConnectionStorage.getPlayerHero(client));

        return item.toString() + " consumed.";
    }

    private String moveHeroToMinionField(Hero initiator, Position currentPosition, Position newPosition) {
        Minion enemyMinion = staticObjectsStorage.getMinion();
        String battleString = getBattleString(initiator, enemyMinion);

        if (battleWithMinion(initiator, enemyMinion)) {
            moveHeroToPosition(initiator, currentPosition, newPosition);
            return battleString + "\nYou WON!";
        } else {
            return battleString + "\nYou LOST!";
        }
    }

    private boolean battleWithMinion(Hero initiator, Minion enemy) {
        while(true){
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

    public String battleWithPlayer(Hero initiator, Hero enemy) {

        StringBuilder battleResult = new StringBuilder("BATTLE: ")
            .append(initiator.toString())
            .append(" VS ")
            .append(enemy.toString());

        while(true){
            enemy.takeDamage(initiator.attack());
            if(!enemy.isAlive()) {
                initiator.gainExperience(50);
                if (enemy.getBackpack().size() == 0) {
                    map.changeGivenFieldByCoordinatesSymbol(enemy.getPositionOnMap().getCoordinate(), Map.FREE_FIELD_SYMBOL);
                } else {
                    dropTreasureFromHero(enemy);
                }
                battleResult.append("\n").append(initiator.getName()).append(" has won!");
                break;
            }
            initiator.takeDamage(enemy.attack());
            if(!initiator.isAlive()) {
                enemy.gainExperience(50);
                if (initiator.getBackpack().size() == 0) {
                    map.changeGivenFieldByCoordinatesSymbol(initiator.getPositionOnMap().getCoordinate(), Map.FREE_FIELD_SYMBOL);
                } else {
                    dropTreasureFromHero(initiator);
                }
                battleResult.append("\n").append(enemy.getName()).append(" has won!");
                break;
            }
        }
        return battleResult.toString();
    }

    public String tradeWithPlayer(Hero initiator, Hero otherHero, int treasureIndex) {
        Treasure treasureToTrade = initiator.getBackpack().getTreasureAt(treasureIndex);
        otherHero.getBackpack().addTreasure(treasureToTrade);
        return "Traded " + treasureToTrade.toString() + " with " + otherHero.getName();
    }

    private String getBattleString(Hero initiator, Actor enemy) {
        return "BATTLE: " + initiator.toString() + " VS " + enemy.toString();
    }

    private char getHeroSymbol(Hero hero) {
        return hero.getSymbolToVisualizeOnMap();
    }

    private String moveHeroToPosition(Hero hero, Position oldPosition, Position newPosition) {

        map.changeGivenFieldByCoordinatesSymbol(oldPosition.getCoordinate(), Map.FREE_FIELD_SYMBOL);
        map.changeGivenFieldByCoordinatesSymbol(newPosition.getCoordinate(), getHeroSymbol(hero));

        hero.setPositionOnMap(newPosition.getCoordinate());

        return "Hero moved successfully.";
    }

    private void dropTreasureFromHero(Hero hero) {
        Treasure treasureToDrop = hero.getBackpack().getTreasureAt(0);
        staticObjectsStorage.addTreasure(treasureToDrop);
        map.changeGivenFieldByCoordinatesSymbol(hero.getPositionOnMap().getCoordinate(), Treasure.SYMBOL_TO_VISUALIZE_ON_MAP);
    }
}
