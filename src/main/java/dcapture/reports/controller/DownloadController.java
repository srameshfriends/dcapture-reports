package dcapture.reports.controller;

import dcapture.reports.util.MessageException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(value = "/download")
public class DownloadController {
    public static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 256 * 1024;
    private static Path tempReportFolder;
    private final Map<Long, Path> cacheReportFileMap;

    public DownloadController() {
        cacheReportFileMap = new HashMap<>();
        deleteReportFiles();
    }

    public static Path getTempReportFolder() {
        if (tempReportFolder == null) {
            String pathText = System.getProperty("java.io.tmpdir");
            Path folder = Paths.get(pathText, "srimalar", "reports");
            if (!Files.exists(folder)) {
                try {
                    folder = Files.createDirectories(folder);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            tempReportFolder = folder;
        }
        return tempReportFolder;
    }

    public static String saveRandomPdfReport(JasperPrint jasperPrint, String reportName) {
        String name = reportName + "-" + UUID.randomUUID().toString().replaceAll("-", "") + ".pdf";
        File file = getTempReportFolder().resolve(name).toFile();
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            JRPdfExporter jrPdfExporter = new JRPdfExporter();
            jrPdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            jrPdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            jrPdfExporter.setConfiguration(configuration);
            jrPdfExporter.exportReport();
            outputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return file.getName();
    }

    @GetMapping("/report")
    public @ResponseBody StreamingResponseBody download(@RequestParam("href") String href,
                                                        HttpServletResponse response) {
        if (href == null) {
            throw new MessageException("jasper.link.empty");
        }
        Path reportPath = getTempReportFolder().resolve(href);
        if (!Files.exists(reportPath)) {
            throw new MessageException("jasper.report.notCreated");
        }
        String contentDisposition = "inline; filename=" + href;
        String format = "pdf";
        int index = href.lastIndexOf(".");
        if (href.length() > (index + 1)) {
            format = href.substring(index + 1);
        }
        if (format.contains("pdf")) {
            response.setContentType("application/pdf");
        } else {
            response.setContentType("application/" + format);
        }
        try {
            response.setHeader("Content-disposition", contentDisposition);
            response.setHeader("Content-Encoding", ENCODING);
            response.setHeader("Content-disposition", "inline; filename=" + href);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLength(((int) Files.size(reportPath)));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.generate.error", href, ex.getMessage());
        }
        cacheAndDeleteReportFiles(reportPath);
        return outputStream -> {
            try (FileInputStream fileInputStream = new FileInputStream(reportPath.toFile())) {
                int nRead;
                byte[] data = new byte[BUFFER_SIZE];
                while ((nRead = fileInputStream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, nRead);
                }
            }
        };
    }

    private void deleteReportFiles() {
        Path root = getTempReportFolder();
        long deleteTime = System.currentTimeMillis() - (1000 * 60 * 10); // 10 minutes
        List<Path> deletePaths = new ArrayList<>();
        try (Stream<Path> pathStream = Files.list(root)) {
            pathStream.forEach(path -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    long createdOnTime = attributes.creationTime().to(TimeUnit.MILLISECONDS);
                    if (createdOnTime < deleteTime) {
                        deletePaths.add(path);
                    } else {
                        cacheReportFileMap.put(createdOnTime, path);
                    }
                } catch (IOException ioe) {
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
            }
        });
    }

    private void cacheAndDeleteReportFiles(Path cacheReportPath) {
        long deleteTime = System.currentTimeMillis() - (1000 * 60 * 10); // 10 minutes
        Map<Long, Path> deletePathMap = new HashMap<>();
        cacheReportFileMap.forEach((createdOnTime, deletePath) -> {
            if (createdOnTime < deleteTime) {
                deletePathMap.put(createdOnTime, deletePath);
            }
        });
        deletePathMap.forEach((createTime, delPath) -> {
            try {
                Files.deleteIfExists(delPath);
                cacheReportFileMap.remove(createTime);
            } catch (IOException ex) {
                log.info("ERROR : Delete cached report file, " + ex.getMessage());
            }
        });
        try {
            BasicFileAttributes attributes = Files.readAttributes(cacheReportPath, BasicFileAttributes.class);
            long createdOnTime = attributes.creationTime().to(TimeUnit.MILLISECONDS);
            cacheReportFileMap.put(createdOnTime, cacheReportPath);
        } catch (IOException ioe) {
            log.info("ERROR : Cache report file, " + ioe.getMessage());
        }
    }
}
