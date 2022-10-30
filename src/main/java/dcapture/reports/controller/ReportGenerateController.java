package dcapture.reports.controller;

import dcapture.reports.jasper.JasperSource;
import dcapture.reports.jasper.JsonJRDataSource;
import dcapture.reports.repository.JasperSourceRepository;
import dcapture.reports.util.MessageException;
import dcapture.reports.util.ReportHref;
import jakarta.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping(value = "/generate")
public class ReportGenerateController {
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");

    private final JasperSourceRepository jasperSourceRepository;

    @Autowired
    public ReportGenerateController(JasperSourceRepository jasperSourceRepository) {
        this.jasperSourceRepository = jasperSourceRepository;
    }

    @PostMapping("/pdf/file")
    public @ResponseBody StreamingResponseBody generate(@RequestParam("report_name") String reportName,
                                                        @RequestBody String jasperData,
                                                        HttpServletResponse response) {
        JasperSource jasperSource = jasperSourceRepository.findByName(reportName);
        if (jasperSource == null) {
            throw new MessageException("jasper.source.empty");
        }
        JsonObject dataFormat = jasperSourceRepository.getJasperDataFormat(jasperSource);
        JsonJRDataSource jsonJRDataSource = new JsonJRDataSource(jasperData, dataFormat);
        JasperReport jasperReport = jasperSourceRepository.getJasperReport(reportName);
        try {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jsonJRDataSource.getParameters(),
                    jsonJRDataSource);
            return sendPdfResponse(reportName, jasperPrint, response);
        } catch (JRException ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.generate.error", reportName, ex.getMessage());
        }
    }

    @PostMapping("/pdf/link")
    public @ResponseBody ResponseEntity<ReportHref> generateLink(@RequestParam("report_name") String reportName,
                                                                 @RequestBody String jasperData) {
        JasperSource jasperSource = jasperSourceRepository.findByName(reportName);
        if (jasperSource == null) {
            throw new MessageException("jasper.source.empty");
        }
        JsonObject dataFormat = jasperSourceRepository.getJasperDataFormat(jasperSource);
        JsonJRDataSource jsonJRDataSource = new JsonJRDataSource(jasperData, dataFormat);
        try {
            JasperReport jasperReport = jasperSourceRepository.getJasperReport(reportName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jsonJRDataSource.getParameters(),
                    jsonJRDataSource);
            String link = DownloadController.saveRandomPdfReport(jasperPrint, reportName);
            return ReportHref.get(jasperSource, link);
        } catch (JRException ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.generate.error", reportName, ex.getMessage());
        }
    }

    private StreamingResponseBody sendPdfResponse(String reportName, JasperPrint jasperPrint,
                                                  HttpServletResponse response) {
        String contentDisposition = timeFormat.format(new Date());
        contentDisposition = contentDisposition.replaceAll("-", "").replaceAll("\\s+", "");
        contentDisposition = "inline; filename=" + reportName + "-" + contentDisposition + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", contentDisposition);
        response.setHeader("Content-Encoding", DownloadController.ENCODING);
        response.setStatus(HttpServletResponse.SC_OK);
        return outputStream -> {
            JRPdfExporter jrPdfExporter = new JRPdfExporter();
            jrPdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            jrPdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            jrPdfExporter.setConfiguration(configuration);
            try {
                jrPdfExporter.exportReport();
            } catch (JRException ex) {
                ex.printStackTrace();
                throw new MessageException("jasper.generate.error", reportName, ex.getMessage());
            }
        };
    }
}
