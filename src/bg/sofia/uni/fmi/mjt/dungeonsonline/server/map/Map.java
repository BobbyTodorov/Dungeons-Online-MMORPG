package bg.sofia.uni.fmi.mjt.dungeonsonline.server.map;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.storage.StaticObjectsStorage;

import java.util.List;

public final class Map {

    private static Map instance = null;

    // static map field symbols
    public final static char UNINITIALIZED_FIELD_SYMBOL = '\0';
    public final static char FREE_FIELD_SYMBOL = '.';
    public final static char OBSTACLE_FIELD_SYMBOL = '#';

    private final static int MAP_WIDTH = 60;
    private final static int MAP_HEIGHT = 10;

    private final char[][] matrix;

    private Map(StaticObjectsStorage staticObjectsStorage) {
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

    public void changeGivenFieldByCoordinatesSymbol(Coordinate fieldCoordinate, char newFieldSymbol) {
        matrix[fieldCoordinate.x()][fieldCoordinate.y()] = newFieldSymbol;
    }

    public char getFieldSymbol(Coordinate coordinate) {
        return matrix[coordinate.x()][coordinate.y()];
    }

    private void setGivenObjectsSymbolAtRandomFields(List<? extends Visualizable> visualizableObjects) {
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
