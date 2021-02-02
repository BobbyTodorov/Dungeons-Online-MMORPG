package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.attributes;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.movement.Direction;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.map.Coordinate;

public class Position {
    Coordinate coordinate;

    public Position(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    /**
     * @param oldPosition old position
     * @param direction desired direction to new position
     * @return newPosition or Position(0,0) if direction is null
     */
    public static Position createPosition(Position oldPosition, Direction direction){
        Position newPosition;

        switch (direction) {
            case UP -> newPosition = new Position(new Coordinate(oldPosition.coordinate.x(), oldPosition.coordinate.y() - 1));
            case DOWN -> newPosition = new Position(new Coordinate(oldPosition.coordinate.x(), oldPosition.coordinate.y() + 1));
            case LEFT -> newPosition = new Position(new Coordinate(oldPosition.coordinate.x() - 1, oldPosition.coordinate.y()));
            case RIGHT -> newPosition = new Position(new Coordinate(oldPosition.coordinate.x() + 1, oldPosition.coordinate.y()));
            default -> { return new Position(new Coordinate(0,0)); }
        }

        return newPosition;
    }
}
