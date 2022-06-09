package dcapture.reports.embedded;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dcapture.reports.jasper.JsonJRDataSource;
import dcapture.reports.jasper.ReportUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public class Test {
    public static void main(String... args) {
        ReportUtils.setJRRootFolder(Paths.get("C:\\workspace\\jasper-reports"));
        ObjectNode dataFormat = ReportUtils.getDataFormat("pos-invoice");
        ObjectMapper mapper = new ObjectMapper();
        try {
            ObjectNode data = mapper.readValue(Paths.get(
                    "C:\\Users\\admin\\Downloads\\invoice-delete\\pos-invoice.json").toFile(), ObjectNode.class);
            JasperReport jasperReport = ReportUtils.getJasperReport("pos-invoice");
            JsonJRDataSource dataSource = new JsonJRDataSource(data, dataFormat);
            JasperPrint jasperPrint
                    = JasperFillManager.fillReport(jasperReport, dataSource.getParameters(), dataSource);
            String fileName = UUID.randomUUID().toString().replaceAll("-", "");
            File file = Paths.get("C:\\Users\\admin\\Downloads\\invoice-delete", fileName + ".pdf").toFile();
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                JRPdfExporter jrPdfExporter = new JRPdfExporter();
                jrPdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                jrPdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
                jrPdfExporter.setConfiguration(configuration);
                jrPdfExporter.exportReport();
                outputStream.flush();
            }
        } catch (JRException | IOException exe) {
            exe.printStackTrace();
        }
    }
}
