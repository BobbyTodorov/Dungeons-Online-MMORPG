package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.validator;

public final class ArgumentValidator {

    private final static String MUST_BE_POSITIVE_MESSAGE = "Given argument(s) must be positive.";
    private final static String MUST_BE_NON_NEGATIVE_MESSAGE = "Given argument(s) must be non-negative.";
    private final static String MUST_BE_NOT_NULL_MESSAGE = "Given argument(s) must be not null.";

    private ArgumentValidator() {}

    public static void checkForNonNegativeArguments(int... args) {
        for (int arg : args) {
            if (arg < 0) {
                throw new IllegalArgumentException(MUST_BE_NON_NEGATIVE_MESSAGE);
            }
        }
    }

    public static void checkForPositiveArguments(int... args) {
        for (int arg : args) {
            if (arg <= 0) {
                throw new IllegalArgumentException(MUST_BE_POSITIVE_MESSAGE);
            }
        }
    }

    public static void checkForNullArguments(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException(MUST_BE_NOT_NULL_MESSAGE);
            }
        }
    }
}
