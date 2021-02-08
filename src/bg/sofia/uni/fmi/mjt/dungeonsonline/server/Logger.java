package bg.sofia.uni.fmi.mjt.dungeonsonline.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final String FILE_NAME = "log.txt";
    private static final String DATE_FORMAT_PATTERN = "dd-MM-yyyy HH:mm:ss";

    private final BufferedWriter writer;
    private Date date;
    SimpleDateFormat formatter;

    private static Logger instance = null;

    private Logger() {
        try {
            writer = new BufferedWriter(new FileWriter(FILE_NAME));
            date = new Date();
            formatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot instantiate Logged", e);
        }
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }

        return instance;
    }

    public void log(String data) throws IOException {
        writer.write(formatter.format(date) + data + System.lineSeparator() + " ");
        writer.flush();
    }
}
