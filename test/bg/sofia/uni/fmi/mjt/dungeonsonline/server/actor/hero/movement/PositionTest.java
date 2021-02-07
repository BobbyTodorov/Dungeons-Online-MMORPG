package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero.movement;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Coordinate;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class PositionTest {

    @Mock
    Coordinate coordinateMock;

    @Test
    public void testCreatePositionToTheLeft() {
        //TODO
        Position tempPosition = new Position(coordinateMock);
        Coordinate currentCoordinate = tempPosition.getCoordinate();

        Position testPosition = Position.createPosition(tempPosition, Direction.LEFT);

        //when(Coordinate::new).thenReturn(new Coordinate(tempPosition.coordinate.x() - 1, tempPosition.coordinate.y()));

        //assertEquals("createPosition should return correct position to the left", )
    }
}
