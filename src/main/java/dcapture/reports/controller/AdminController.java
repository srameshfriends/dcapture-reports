package dcapture.reports.controller;

import dcapture.reports.util.IOFolderFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(value = "/")
public class AdminController {

    @GetMapping("/disk/clean")
    public @ResponseBody String diskClean() {
        StringBuilder result = new StringBuilder("Disk clean completed successfully.");
        Path root = IOFolderFileUtil.getTempDirectory();
        long deleteTime = System.currentTimeMillis() - (1000 * 60 * 60); // 1 hour
        List<Path> deletePaths = new ArrayList<>();
        try (Stream<Path> pathStream = Files.list(root)) {
            pathStream.forEach(path -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    long createdOnTime = attributes.creationTime().to(TimeUnit.MILLISECONDS);
                    if (createdOnTime < deleteTime) {
                        deletePaths.add(path);
                    }
                } catch (IOException ioe) {
                    result.append("ERROR : Delete report file info, ").append(ioe.getMessage()).append("\n");
                    log.info("ERROR : Delete report file info, " + ioe.getMessage());
                }
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        deletePaths.forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException epn) {
                log.info("ERROR : Delete report file, " + epn.getMessage());
                result.append("ERROR : Delete report file, ").append(epn.getMessage()).append("\n");
            }
        });
        return result.toString();
    }
}
