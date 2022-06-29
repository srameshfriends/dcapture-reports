package dcapture.reports.embedded;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Release {
    private static final String SOURCE_DIR = "C:\\Users\\admin\\JaspersoftWorkspace\\dcapture-jasper";
    private static final String TARGET_DIR = "C:\\workspace\\jasper-reports\\classes";
    private static final String JASPER_REPORTS_DIR = "C:\\workspace\\dcapture-reports\\jasper";
    private static final String JASPER_CONFIG_SOURCE = "C:\\workspace\\jasper-reports\\config";
    private static final String JASPER_CONFIG_FILE_NAME = "jasper-reports.json";

    private static final String[] JASPER_FILES = new String[]{
            "pos-invoice.jrxml", "pos-invoice.jasper"
    };

    public static void main(String... args) {
        System.out.println("========== *** ==========");
        Arrays.stream(JASPER_FILES).forEach(str -> {
            deleteFile(str);
            copyFile(str);
            deleteFileRepository(str);
            copyFileRepository(str);
            deleteConfigFile();
            copyConfigFile();
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

    private static void deleteFileRepository(String target) {
        Path path = Paths.get(JASPER_REPORTS_DIR, target);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
                System.out.println("Deleted : " + path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void deleteConfigFile() {
        Path path = Paths.get(JASPER_REPORTS_DIR, JASPER_CONFIG_FILE_NAME);
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
                System.out.println("COPY FROM : " + path + " \t COPY TO : " + toFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void copyFileRepository(String source) {
        Path path = Paths.get(SOURCE_DIR, source);
        if (Files.exists(path)) {
            try {
                Path toFile = Paths.get(JASPER_REPORTS_DIR, path.toFile().getName());
                Files.copy(path, toFile);
                System.out.println("REPOSITORY COPY FROM : " + path + " \t COPY TO : " + toFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void copyConfigFile() {
        Path path = Paths.get(JASPER_CONFIG_SOURCE, JASPER_CONFIG_FILE_NAME);
        if (Files.exists(path)) {
            try {
                Path toFile = Paths.get(JASPER_REPORTS_DIR, JASPER_CONFIG_FILE_NAME);
                Files.copy(path, toFile);
                System.out.println("REPOSITORY COPY FROM : " + path + " \t COPY TO : " + toFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
