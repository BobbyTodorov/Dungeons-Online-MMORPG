package dungeonsonline.server.actor.hero.movement;

import dungeonsonline.server.map.Coordinate;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PositionTest {

    @Test
    public void testCreatePositionToTheLeft() {
        Position oldPosition = new Position(new Coordinate(1, 1));

        Position newPosition = Position.createPosition(oldPosition, Direction.LEFT);

        Assert.assertEquals("createPosition should return correct position to the left",
            new Coordinate(0, 1), newPosition.coordinate());
    }

    @Test
    public void testCreatePositionToTheRight() {
        Position oldPosition = new Position(new Coordinate(1, 1));

        Position newPosition = Position.createPosition(oldPosition, Direction.RIGHT);

        Assert.assertEquals("createPosition should return correct position to the right",
            new Coordinate(2, 1), newPosition.coordinate());
    }

    @Test
    public void testCreatePositionToUp() {
        Position oldPosition = new Position(new Coordinate(1, 1));

        Position newPosition = Position.createPosition(oldPosition, Direction.UP);

        Assert.assertEquals("createPosition should return correct position to up",
            new Coordinate(1, 0), newPosition.coordinate());
    }

    @Test
    public void testCreatePositionToDown() {
        Position oldPosition = new Position(new Coordinate(1, 1));

        Position newPosition = Position.createPosition(oldPosition, Direction.DOWN);

        Assert.assertEquals("createPosition should return correct position to down",
            new Coordinate(1, 2), newPosition.coordinate());
    }
}
