package edu.umd.cs.findbugs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author colin
 */
public class LogUtil {

    private static Map<String, OutputStreamWriter> writerMap = new ConcurrentHashMap<>();
    private static final String defaultLogFile = "spotbugs";
    private static final LogLevel DEFAULT_LOG_LEVEL;

    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3);
        private final int level;

        private LogLevel(int level) {
            this.level = level;
        }

        public int getValue() {
            return this.level;
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                writerMap.forEach((key, value) -> {
                    try {
                        value.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
        final String logLevelEnv = System.getenv("LOG_LEVEL");
        String levelStr = Stream.of(LogLevel.values())
            .map(Enum::name)
            .filter(i -> i.equalsIgnoreCase(logLevelEnv))
            .findAny().orElse("INFO");
        DEFAULT_LOG_LEVEL = LogLevel.valueOf(levelStr);
    }

    public static String getProjectName(String name) {
        if (null == name) {
            return defaultLogFile;
        }
        return name;
    }

    public static Path getLogFilePath(String name) throws IOException {
        String projectName = name;
        Path logFolderPath = Paths.get(Paths.get("").toAbsolutePath().toString(), ".spotbugs-logs");
        if (!Files.exists(logFolderPath)) {
            Files.createDirectories(logFolderPath);
        }
        if (Files.exists(Paths.get(logFolderPath.toString(), "nolog"))) {
            return null;
        }
        return Paths.get(logFolderPath.toString(), projectName.replaceAll(" ", "-"));
    }

    public synchronized static void log(LogLevel level, String name, String content) {
        if (level.level < DEFAULT_LOG_LEVEL.level) {
            return;
        }
        String key = getProjectName(name);
        if (null == key) {
            return;
        }
        try {
            if (!writerMap.containsKey(key)) {
                Path path = getLogFilePath(name);
                if (null == path) {
                    return;
                }
                if (!writerMap.containsKey(key)) {
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()));
                    writerMap.put(key, writer);
                }
            }
            writerMap.get(key).append(new Date().toString()).append(" ").append(key)
                .append(" ").append(level.name()).append(" ").append(content).append('\n');
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized static void log(String name, String content) {
        log(LogLevel.INFO, name, content);
    }
}
