package dungeonsonline.server.validator;

import org.junit.Test;

public class ArgumentValidatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCheckForNonNegativeArgumentsWithNullArgument() {
        ArgumentValidator.checkForNullArguments("123", null);
    }

    @Test
    public void testCheckForNonNegativeArgumentsWithoutNullArgument() {
        ArgumentValidator.checkForNullArguments("123", 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckForPositiveArgumentsWithZeroArgument() {
        ArgumentValidator.checkForPositiveArguments(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckForPositiveArgumentsWithNegativeArgument() {
        ArgumentValidator.checkForPositiveArguments(-1);
    }

    @Test
    public void testCheckForPositiveArgumentsWithPositiveArgument() {
        ArgumentValidator.checkForPositiveArguments(1, 2, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckForNonNegativeArgumentsWithNegativeArgument() {
        ArgumentValidator.checkForNonNegativeArguments(-1);
    }

    @Test
    public void testCheckForNonNegativeArgumentsWithZeroAndPositiveArgument() {
        ArgumentValidator.checkForNonNegativeArguments(0, 2, 3);
    }
}
