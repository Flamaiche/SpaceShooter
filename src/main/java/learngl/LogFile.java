package learngl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogFile {
    private static PrintWriter writer;
    private static String filePath;

    public static void init() {
        if (writer != null) return;

        String dir = System.getProperty("user.dir");
        if (dir == null) dir = System.getProperty("user.home");
        if (dir == null) dir = ".";

        Path path = Paths.get(dir, "camera-debug.log");
        filePath = path.toAbsolutePath().normalize().toString();

        try {
            writer = new PrintWriter(new FileWriter(path.toFile(), true), true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }));

            System.err.println("LogFile: writing to " + filePath);
        } catch (IOException e) {
            System.err.println("LogFile.init: " + e.getMessage());
        }
    }

    public static void log(String msg) {
        System.out.println(msg);
        if (writer != null) {
            writer.println(msg);
            writer.flush();
        }
    }

    public static void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format + "%n", args);
        if (writer != null) {
            writer.printf(format + "%n", args);
            writer.flush();
        }
    }
}
