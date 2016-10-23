package excel.accounting.shared;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * FileHelper
 */
public class FileHelper {
    private static File lastAccessFolder;

    public static Path getClassPath(String pathValue) throws Exception {
        pathValue = pathValue == null ? "" : pathValue.trim();
        if (pathValue.startsWith("/") || pathValue.startsWith("\\")) {
            pathValue = pathValue.substring(1, pathValue.length());
        }
        if (pathValue.endsWith("/") || pathValue.endsWith("\\")) {
            pathValue = pathValue.substring(0, pathValue.length() - 1);
        }
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(pathValue);
        return url == null ? null : Paths.get(url.toURI());
    }

    public static File showOpenFileDialogExcel(Stage stage) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Excel", "*.xls", "*.xlsx");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Excel File");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(getLastAccessFolder());
        File openFile = fileChooser.showOpenDialog(stage);
        if (openFile != null) {
            setLastAccessFolder(openFile);
        }
        return openFile;
    }

    public static File showSaveFileDialogExcel(String fileName, Stage stage) {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Excel", "*.xls");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(getLastAccessFolder());
        fileChooser.setInitialFileName(fileName);
        File saveFile = fileChooser.showSaveDialog(stage);
        if (saveFile != null) {
            setLastAccessFolder(saveFile);
        }
        return saveFile;
    }

    public static List<File> getFilesInDirectory(File dir, String extension) {
        final String ext = extension.startsWith(".") ? extension.toLowerCase() : ".".concat(extension).toLowerCase();
        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(ext));
        if (files == null || files.length == 0) {
            return null;
        }
        return Arrays.asList(files);
    }

    private static void setLastAccessFolder(File file) {
        if (file.isFile()) {
            lastAccessFolder = file.getParentFile();
        } else if (file.isDirectory()) {
            lastAccessFolder = file;
        }
    }

    private static File getLastAccessFolder() {
        if (lastAccessFolder == null) {
            File homeFolder = new File(System.getProperty("user.home"));
            if (homeFolder.isDirectory()) {
                lastAccessFolder = homeFolder;
            }
        }
        return lastAccessFolder;
    }

    public static String getFileExtension(File file) {
        String fileName = file.getPath();
        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (i > p) {
            return fileName.substring(i + 1);
        }
        return "";
    }

    public static String getNameWithoutExtension(File file) {
        String ext = getFileExtension(file);
        ext = ".".concat(ext);
        ext = file.getName().replace(ext, "");
        return ext;
    }

    public static List<String> readAllLines(File file) throws Exception {
        List<String> queryList = Files.readAllLines(file.toPath());
        if (queryList.size() == 0) {
            throw new IllegalArgumentException("sql file content is empty " + file.toPath());
        }
        return queryList;
    }
}
