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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(value = "/download")
public class DownloadController {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh-mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 256 * 1024;
    private static Path tempReportFolder;

    public static Path getTempReportFolder() {
        if (tempReportFolder == null) {
            String pathText = System.getProperty("java.io.tmpdir");
            Path folder = Paths.get(pathText, "dcapture", "reports");
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
    public @ResponseBody StreamingResponseBody download(
            @RequestParam(value = "href", defaultValue = "") String href,
            @RequestParam(value = "type", defaultValue = "inline", required = false) String type,
            @RequestParam(value = "prefix", defaultValue = "", required = false) String prefix,
            @RequestParam(value = "suffix", defaultValue = "time", required = false) String suffix, HttpServletResponse response) {
        if (href.isBlank()) {
            throw new MessageException("jasper.link.empty");
        }
        Path reportPath = getTempReportFolder().resolve(href);
        if (!Files.exists(reportPath)) {
            throw new MessageException("jasper.report.notCreated", reportPath.toString());
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
        if (!"inline".equalsIgnoreCase(type) && !"attachment".equalsIgnoreCase(type)) {
            type = "inline";
        }
        prefix = prefix.trim();
        if ("date".equalsIgnoreCase(suffix)) {
            suffix = DATE_FORMAT.format(new Date());
            suffix = suffix.replaceAll("-", "").replaceAll("\\s+", "") + "." + format;
        } else if ("time".equalsIgnoreCase(suffix)) {
            suffix = TIME_FORMAT.format(new Date());
            suffix = suffix.replaceAll("-", "").replaceAll("\\s+", "") + "." + format;
        } else if (!suffix.contains(format)) {
            suffix = suffix + "." + format;
        } else {
            suffix = href;
        }
        type = type.toLowerCase();
        try {
            response.setHeader("Content-disposition", contentDisposition);
            response.setHeader("Content-Encoding", ENCODING);
            response.setHeader("Content-disposition", type + "; filename=" + prefix + suffix);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLength(((int) Files.size(reportPath)));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.generate.error", href, ex.getMessage());
        }
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

    @DeleteMapping("/disk/clean")
    public @ResponseBody String diskClean() {
        StringBuilder result = new StringBuilder("Disk clean completed successfully.");
        Path root = getTempReportFolder();
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
