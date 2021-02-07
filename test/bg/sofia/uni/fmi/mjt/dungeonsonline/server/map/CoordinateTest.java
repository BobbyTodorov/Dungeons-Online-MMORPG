package bg.sofia.uni.fmi.mjt.dungeonsonline.server.map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CoordinateTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRandomCoordinateWithNegativeMinX() {
        Coordinate.createRandomCoordinate(-1, 1, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRandomCoordinateWithNegativeMaxX() {
        Coordinate.createRandomCoordinate(1, -1, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRandomCoordinateWithNegativeMinY() {
        Coordinate.createRandomCoordinate(1, 1, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRandomCoordinateWithNegativeMaxY() {
        Coordinate.createRandomCoordinate(1, 1, 1, -1);
    }

    @Test
    public void testCreateRandomCoordinateSuccess() {
        int minX = 1;
        int maxX = 3;
        int minY = 1;
        int maxY = 3;

        for (int i = 0; i < 50; ++ i) {
            Coordinate coordinate = Coordinate.createRandomCoordinate(minX, maxX, minY, maxY);
            assertTrue("CreateRandomCoordinate returned coordinate out of given bounds",
                coordinate.x() >= minX && coordinate.x() <= maxX
                    && coordinate.y() >= minY && coordinate.y() <= maxY);
        }
    }
}
