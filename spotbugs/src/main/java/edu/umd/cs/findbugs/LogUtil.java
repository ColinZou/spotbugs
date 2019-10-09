package edu.umd.cs.findbugs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author colin
 */
public class LogUtil {
    private static Map<String, OutputStreamWriter> writerMap = new ConcurrentHashMap<>();
    private static final String defaultLogFile = "spot-bugs";

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
    }

    public static String getProjectName(String name) {
        if (null == name) {
            return defaultLogFile;
        }
        return name;
    }

    public static Path getLogFilePath(String name) throws IOException {
        String projectName = name;
        Path logFolderPath = Paths.get(System.getProperty("java.io.tmpdir", "/tmp"), "spot-bugs-logs");
        if (!Files.exists(logFolderPath)) {
            Files.createDirectories(logFolderPath);
        }
        return Paths.get(logFolderPath.toString(), projectName.replaceAll(" ", "-"));
    }

    public synchronized static void log(String name, String content) {
        String key = getProjectName(name);
        if (null == key) {
            return;
        }
        try {
            if (!writerMap.containsKey(key)) {
                Path path = getLogFilePath(name);
                if (!writerMap.containsKey(key)) {
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()));
                    writerMap.put(key, writer);
                }
            }
//            writerMap.get(key).append(new Date().toString()).append(" ").append(key)
//                .append(" : ").append(content).append('\n');
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
