package dcapture.reports.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class IOFolderFileUtil {
    private static Path tempDirectory;

    public static Path getTempDirectory() {
        if (tempDirectory == null) {
            String pathText = System.getProperty("java.io.tmpdir");
            Path dir = Paths.get(pathText, "dcapture", "reports");
            if (!Files.exists(dir)) {
                try {
                    dir = Files.createDirectories(dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            tempDirectory = dir;
        }
        return tempDirectory;
    }

    public static Path getTempRandomFile(String fileType) {
        Path path = getTempDirectory();
        return path.resolve(UUID.randomUUID() + "." + fileType);
    }
}
