package dcapture.reports.controller;

import dcapture.reports.util.IOFolderFileUtil;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/download")
public class DownloadController {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh-mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 256 * 1024;

    public static String saveRandomPdfReport(JasperPrint jasperPrint, String reportName) {
        String name = reportName + "-" + UUID.randomUUID().toString().replaceAll("-", "") + ".pdf";
        File file = IOFolderFileUtil.getTempDirectory().resolve(name).toFile();
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
        Path reportPath = IOFolderFileUtil.getTempDirectory().resolve(href);
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
}
