package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.gameengine;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.attributes.Position;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.hero.Hero;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.map.Coordinate;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.map.Map;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.storage.StaticObjectsStorage;

public class GameEngine {

    private static final StaticObjectsStorage staticObjectsStorage = StaticObjectsStorage.getInstance();
    private static final Map map = Map.getInstance(staticObjectsStorage);

    public String getMapToVisualize() {
        assert map != null;
        return map.getMap();
    }

    public Coordinate summonPlayerHero(Hero hero) {
        assert map != null;
        Coordinate summonedHeroPosition = map.changeRandomFieldWithGivenSymbolToAnother(Map.FREE_FIELD_SYMBOL, getHeroSymbol(hero));
        hero.setPositionOnMap(summonedHeroPosition);
        return summonedHeroPosition;
    }

    public void unSummonPlayerHero(Hero hero) {
        map.changeRandomFieldWithGivenSymbolToAnother(getHeroSymbol(hero), Map.FREE_FIELD_SYMBOL);
    }

    public String moveHero(Hero hero, Direction direction) {
        Position currentHeroPosition = hero.getPositionOnMap();
        Position newHeroPosition = Position.createPosition(currentHeroPosition, direction);

        char newPositionFieldSymbol = map.getFieldSymbol(newHeroPosition.getCoordinate());

        switch (newPositionFieldSymbol) {
            case Map.FREE_FIELD_SYMBOL -> {
                return moveHeroToPosition(hero, currentHeroPosition, newHeroPosition);
            }
            default -> { return "invalid direction"; }
        }
    }

    private String moveHeroToPosition(Hero hero, Position oldPosition, Position newPosition) {
        map.changeGivenFieldByCoordinatesSymbol(oldPosition.getCoordinate(), Map.FREE_FIELD_SYMBOL);
        map.changeGivenFieldByCoordinatesSymbol(newPosition.getCoordinate(), getHeroSymbol(hero));
        hero.setPositionOnMap(newPosition.getCoordinate());

        return "Hero moved successfully.";
    }

    private char getHeroSymbol(Hero hero) {
        return hero.getSymbolToVisualizeOnMap();
    }
}
