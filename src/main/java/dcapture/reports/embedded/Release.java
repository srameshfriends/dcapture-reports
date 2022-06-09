package dcapture.reports.embedded;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Release {
    private static final String SOURCE_DIR = "C:\\Users\\admin\\JaspersoftWorkspace\\dcapture-jasper";
    private static final String TARGET_DIR = "C:\\workspace\\jasper-reports\\classes";

    public static void main(String... args) {
        String[] fileNames = new String[]{
                "pos-invoice.jrxml", "pos-invoice.jasper"
        };
        System.out.println("========== *** ==========");
        Arrays.stream(fileNames).forEach(str -> {
            deleteFile(str);
            copyFile(str);
        });
    }

    private static void deleteFile(String target) {
        Path path = Paths.get(TARGET_DIR, target);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
                System.out.println("Deleted : " + path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void copyFile(String source) {
        Path path = Paths.get(SOURCE_DIR, source);
        if (Files.exists(path)) {
            try {
                Path toFile = Paths.get(TARGET_DIR, path.toFile().getName());
                Files.copy(path, toFile);
                System.out.println("Copy : " + path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
