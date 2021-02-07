package bg.sofia.uni.fmi.mjt.dungeonsonline.server.map;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

import java.util.Random;

public final record Coordinate(int x, int y) {
    public static Coordinate createRandomCoordinate(int minX, int maxX, int minY, int maxY) {
        ArgumentValidator.checkForNonNegativeArguments(minX, maxX, minY, maxY);

        Random random = new Random();
        int x = random.nextInt(maxX - minX) + minX;
        int y = random.nextInt(maxY - minY) + minY;
        return new Coordinate(x, y);
    }
}
