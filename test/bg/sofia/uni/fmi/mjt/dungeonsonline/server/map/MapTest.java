package bg.sofia.uni.fmi.mjt.dungeonsonline.server.map;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.exceptions.OutOfMapBoundsException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MapTest {

    @Mock
    StaticObjectsStorage staticObjectsStorageMock;

    @InjectMocks
    Map map;

    @Test(expected = IllegalArgumentException.class)
    public void testChangeRandomFieldWithGivenSymbolToAnotherWithInvalidFromSymbolArgument() {
        map.changeRandomFieldWithGivenSymbolToAnother('@', '.');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeRandomFieldWithGivenSymbolToAnotherWithInvalidToSymbolArgument() {
        map.changeRandomFieldWithGivenSymbolToAnother('.', '@');
    }

    @Test
    public void testChangeRandomFieldWithGivenSymbolToAnotherSuccess() {
        map.changeRandomFieldWithGivenSymbolToAnother('.', '1');

        String actual = map.matrix().replaceAll("\\.|\n|\r\n|#", "");

        assertEquals("changeRandomFieldWishGivenSymbolToAnother does not work as expected", "1", actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeGivenFieldByCoordinatesSymbolWithInvalidCoordinateArgument() {
        map.changeGivenFieldByCoordinatesSymbol(null, '.');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeGivenFieldByCoordinatesSymbolWithInvalidNewFieldSymbolArgument() {
        map.changeGivenFieldByCoordinatesSymbol(new Coordinate(0, 0), '@');
    }

    @Test(expected = OutOfMapBoundsException.class)
    public void testChangeGivenFieldByCoordinatesSymbolWithOutOfMapCoordinateArgument() {
        map.changeGivenFieldByCoordinatesSymbol(new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE), '.');
    }

    @Test
    public void testChangeGivenFieldByCoordinatesSymbolSuccess() {
        map.changeGivenFieldByCoordinatesSymbol(new Coordinate(3, 0), '1');

        assertEquals("changeGivenFieldByCoordinates does not work as expected", '1', map.matrix().charAt(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFieldSymbolWithNullArgument() {
        map.getFieldSymbol(null);
    }

    @Test(expected = OutOfMapBoundsException.class)
    public void testGetFieldSymbolWithOutOfMapCoordinateArgument() {
        map.getFieldSymbol(new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    public void testGetFieldSymbolSuccess() {
        Coordinate coordinate = new Coordinate(0, 0);
        map.changeGivenFieldByCoordinatesSymbol(coordinate, '2');

        assertEquals("getFieldSymbol must return correct symbol char", '2', map.getFieldSymbol(coordinate));
    }
}
