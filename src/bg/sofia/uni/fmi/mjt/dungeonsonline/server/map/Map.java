package bg.sofia.uni.fmi.mjt.dungeonsonline.server.map;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.minion.IMinion;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.exceptions.OutOfMapBoundsException;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

import java.util.List;

public final class Map {

    private final static String INVALID_SYMBOL_EXCEPTION_MESSAGE = "Invalid symbol %s";
    private final static String OUT_OF_BOUNDS_EXCEPTION_MESSAGE = "Given coordinate is outside of map's bounds." +
        "Current height: %s, Current width: %s";

    // static map field symbols
    public final static char UNINITIALIZED_FIELD_SYMBOL = '\0';
    public final static char FREE_FIELD_SYMBOL = '.';
    public final static char OBSTACLE_FIELD_SYMBOL = '#';

    private final static int MAP_WIDTH = 60;
    private final static int MAP_HEIGHT = 10;

    private final char[][] matrix;

    private static Map instance = null;

    private Map(StaticObjectsStorage staticObjectsStorage) {
        ArgumentValidator.checkForNullArguments(staticObjectsStorage);

        matrix = new char[MAP_WIDTH][MAP_HEIGHT];
        // TODO visualizeGivenObjects(List.of(OBSTACLE_SYMBOL));
        setGivenObjectsSymbolAtRandomFields(staticObjectsStorage.getMinions());
        setGivenObjectsSymbolAtRandomFields(staticObjectsStorage.getTreasures());
        setUninitializedFieldsToFree();
    }

    public static Map getInstance(StaticObjectsStorage staticObjectsStorage) {
        if (instance == null) {
            instance = new Map(staticObjectsStorage);
        }

        return instance;
    }

    public String getMap() {
        StringBuilder matrixAsString = new StringBuilder();
        for (int i = 0; i < MAP_HEIGHT; ++i) {
            for (int j = 0; j < MAP_WIDTH; ++j) {
                matrixAsString.append(matrix[j][i]);
            }
            matrixAsString.append(System.lineSeparator());
        }
        return matrixAsString.toString();
    }

    public Coordinate changeRandomFieldWithGivenSymbolToAnother(char fromSymbol, char toSymbol) {
        if (!isValidSymbol(fromSymbol)) {
            throw new IllegalArgumentException(String.format(INVALID_SYMBOL_EXCEPTION_MESSAGE, fromSymbol));
        }
        if (!isValidSymbol(toSymbol)) {
            throw new IllegalArgumentException(String.format(INVALID_SYMBOL_EXCEPTION_MESSAGE, toSymbol));
        }

        Coordinate randomCoordinate;
        char randomField;
        do {
            randomCoordinate = Coordinate.createRandomCoordinate(0, MAP_WIDTH, 0, MAP_HEIGHT);
            randomField = getFieldSymbol(randomCoordinate);
        }
        while (randomField != fromSymbol);
        changeGivenFieldByCoordinatesSymbol(randomCoordinate, toSymbol);
        return randomCoordinate;
    }

    public void changeGivenFieldByCoordinatesSymbol(Coordinate coordinate, char newFieldSymbol) {
        ArgumentValidator.checkForNullArguments(coordinate);

        if (!isValidSymbol(newFieldSymbol)) {
            throw new IllegalArgumentException(String.format(INVALID_SYMBOL_EXCEPTION_MESSAGE, newFieldSymbol));
        }

        try {
            matrix[coordinate.x()][coordinate.y()] = newFieldSymbol;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new OutOfMapBoundsException(String.format(OUT_OF_BOUNDS_EXCEPTION_MESSAGE, MAP_HEIGHT, MAP_WIDTH));
        }
    }

    public char getFieldSymbol(Coordinate coordinate) {
        ArgumentValidator.checkForNullArguments(coordinate);

        char fieldSymbol;
        try {
            fieldSymbol = matrix[coordinate.x()][coordinate.y()];
        } catch (ArrayIndexOutOfBoundsException e) {
        throw new OutOfMapBoundsException(String.format(OUT_OF_BOUNDS_EXCEPTION_MESSAGE, MAP_HEIGHT, MAP_WIDTH));
    }

        return fieldSymbol;
    }

    private boolean isValidSymbol(char symbol) {
        return symbol == UNINITIALIZED_FIELD_SYMBOL
            || symbol == FREE_FIELD_SYMBOL
            || symbol == OBSTACLE_FIELD_SYMBOL
            || symbol == Treasure.SYMBOL_TO_VISUALIZE_ON_MAP
            || symbol == IMinion.SYMBOL_TO_VISUALIZE_ON_MAP
            || symbol == '1' || symbol == '2' || symbol == '3'
            || symbol == '4' || symbol == '5' || symbol == '6'
            || symbol == '7' || symbol == '8' || symbol == '9';
    }

    private void setGivenObjectsSymbolAtRandomFields(List<? extends Visualizable> visualizableObjects) {
        ArgumentValidator.checkForNullArguments(visualizableObjects);

        for (Object object : visualizableObjects) {
            changeRandomFieldWithGivenSymbolToAnother(UNINITIALIZED_FIELD_SYMBOL, ((Visualizable) object).getSymbolToVisualizeOnMap());
        }
    }

    private void setUninitializedFieldsToFree() {
        for (int i = 0; i < MAP_WIDTH; ++i) {
            for (int j = 0; j < MAP_HEIGHT ; ++j) {
                if (matrix[i][j] == UNINITIALIZED_FIELD_SYMBOL) {
                    matrix[i][j] = FREE_FIELD_SYMBOL;
                }
            }
        }
    }
}
