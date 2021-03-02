package dungeonsonline.dungeonsclient.ui;

public class UserInterfaceImpl implements UserInterface {

    private final static String moveLeft = "l";
    private final static String moveRight = "r";
    private final static String moveUp = "u";
    private final static String moveDown = "d";
    private final static String backpackOpen = "bp";
    private final static String disconnect = "dc";


    private static UserInterfaceImpl instance;

    private UserInterfaceImpl() {};

    public static UserInterfaceImpl getInstance() {
        if (instance == null) {
            instance = new UserInterfaceImpl();
        }

        return instance;
    }

    @Override
    public String commands() {
        return "Movement: "
            + "left [" + moveLeft + "] - "
            + "right [" + moveRight + "] - "
            + "up [" + moveUp + "] - "
            + "down [" + moveDown + "] - "
            + "open backpack [" + backpackOpen + "] - "
            + "disconnect [" + disconnect + "]"
            + System.lineSeparator();
    }
}
