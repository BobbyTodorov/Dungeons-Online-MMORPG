package dungeonsonline.server.map;

import dungeonsonline.server.actor.minion.IMinion;
import dungeonsonline.server.map.exceptions.OutOfMapBoundsException;
import dungeonsonline.server.storage.StaticObjectsStorage;
import dungeonsonline.server.treasure.Treasure;
import dungeonsonline.server.validator.ArgumentValidator;

import java.util.List;
import java.util.Random;

public class Map {

    private final static String INVALID_SYMBOL_EXCEPTION_MESSAGE = "Invalid symbol %s";
    private final static String OUT_OF_BOUNDS_EXCEPTION_MESSAGE =
        "Given coordinate is outside of map's bounds. Current height: %s, Current width: %s";

    // static map field symbols
    public final static char UNINITIALIZED_FIELD_SYMBOL = '\0';
    public final static char FREE_FIELD_SYMBOL = '.';
    public final static char OBSTACLE_FIELD_SYMBOL = '#';

    private final static int MAP_WIDTH = 30;
    private final static int MAP_HEIGHT = 10;

    private final static int NUMBER_OF_WALLS = 20;
    private final static int MAX_WALL_LENGTH = 5;

    private final char[][] matrix;

    public Map(StaticObjectsStorage staticObjectsStorage) {
        matrix = new char[MAP_WIDTH][MAP_HEIGHT];

        createWalls();
        setGivenObjectsSymbolAtRandomFields(staticObjectsStorage.getMinions());
        setGivenObjectsSymbolAtRandomFields(staticObjectsStorage.getTreasures());
        setUninitializedFieldsToFree();
    }

    /**
     * @return string, containing the map's matrix, that is ready to be visualized
     */
    public String matrix() {
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
            changeRandomFieldWithGivenSymbolToAnother(UNINITIALIZED_FIELD_SYMBOL,
                ((Visualizable) object).getSymbolToVisualizeOnMap());
        }
    }

    private void setUninitializedFieldsToFree() {
        for (int i = 0; i < MAP_WIDTH; ++i) {
            for (int j = 0; j < MAP_HEIGHT; ++j) {
                if (matrix[i][j] == UNINITIALIZED_FIELD_SYMBOL) {
                    matrix[i][j] = FREE_FIELD_SYMBOL;
                }
            }
        }
    }

    private void createWalls() {
        int wallsCounter = NUMBER_OF_WALLS;

        while (wallsCounter > 0) {
            int wallLength = new Random().nextInt(MAX_WALL_LENGTH);

            int randHorizontalStartPos = new Random().nextInt(MAP_WIDTH);
            int randVerticalStartPos = new Random().nextInt(MAP_HEIGHT);

            boolean randomOrientation = new Random().nextBoolean();

            try {
                if (randomOrientation) {
                    createHorizontalWall(wallLength, randHorizontalStartPos, randVerticalStartPos);
                } else {
                    createVerticalWall(wallLength, randHorizontalStartPos, randVerticalStartPos);
                }
            } catch (Exception ignored) { }

            wallsCounter--;
        }
    }

    private void createHorizontalWall(int wallLength, int horizontalStartPos, int verticalStartPos) {
        int modifiedHorizontalWallLength = wallLength * ((MAP_WIDTH - MAP_HEIGHT) / 10);
        for (int i = 0; i < modifiedHorizontalWallLength; ++i) {
            if (matrix[horizontalStartPos + i][verticalStartPos] == UNINITIALIZED_FIELD_SYMBOL) {
                matrix[horizontalStartPos + i][verticalStartPos] = OBSTACLE_FIELD_SYMBOL;
            }
        }
    }

    private void createVerticalWall(int wallLength, int horizontalStartPos, int verticalStartPos) {
        for (int i = 0; i < wallLength; ++i) {
            if (matrix[horizontalStartPos][verticalStartPos + i] == UNINITIALIZED_FIELD_SYMBOL) {
                matrix[horizontalStartPos][verticalStartPos + i] = OBSTACLE_FIELD_SYMBOL;
            }
        }
    }
}
