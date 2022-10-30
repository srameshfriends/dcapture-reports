package dcapture.reports.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.springframework.core.ArangoOperations;
import dcapture.reports.jasper.JasperSource;
import dcapture.reports.repository.JasperSourceRepository;
import dcapture.reports.util.LocaleMessage;
import dcapture.reports.util.MessageException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JasperSourceServiceImpl implements JasperSourceRepository {
    private static Map<String, JasperReport> jasperReportMap;
    private static Map<String, JsonObject> jasperDataFormatMap;
    private final ArangoOperations arangoOperations;
    private final LocaleMessage localeMessage;
    @Value("${jasper.source_folder}")
    private String jasperSourceFolder;
    private Path jasperSourcePath;

    @Autowired
    public JasperSourceServiceImpl(ArangoOperations arangoOperations, LocaleMessage localeMessage) {
        this.arangoOperations = arangoOperations;
        this.localeMessage = localeMessage;
    }

    private static synchronized void deleteJasperReport(Path jasperPath, String reportName) {
        try {
            Path jrXml = jasperPath.resolve(reportName + ".jrxml");
            if (Files.exists(jrXml)) {
                Files.delete(jrXml);
            }
            Path jasper = jasperPath.resolve(reportName + ".jasper");
            if (Files.exists(jasper)) {
                Files.delete(jasper);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.delete.error", reportName, ex.getMessage());
        }
    }

    private static synchronized JasperReport loadJasperReport(Path classPath, String reportName) throws JRException {
        Path jasperFile = classPath.resolve(reportName + ".jasper");
        if (Files.exists(jasperFile)) {
            return (JasperReport) JRLoader.loadObjectFromFile(jasperFile.toString());
        }
        throw new MessageException("jasper.report.notCreated");
    }

    @Override
    @PostConstruct
    public void initialize() {
        jasperReportMap = new HashMap<>();
        jasperDataFormatMap = new HashMap<>();
        jasperSourcePath = getJasperSourceFolder();
        if (!Files.exists(jasperSourcePath)) {
            try {
                jasperSourcePath = Files.createDirectories(jasperSourcePath);
                log.info(localeMessage.getMessage("jasper.class_path", jasperSourcePath));
            } catch (IOException ex) {
                log.info(localeMessage.getMessage("jasper.class_path", "ERROR : " + ex.getMessage()));
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Iterable<JasperSource> findAll() {
        return arangoOperations.findAll(JasperSource.class);
    }

    @Override
    public JasperSource findByName(String reportName) {
        JasperSource result = null;
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("report_name", reportName);
        String query = "FOR doc IN jasper_source FILTER doc.report_name == @report_name RETURN doc";
        try (ArangoCursor<JasperSource> cursor = arangoOperations.query(query, bindVars, JasperSource.class)) {
            if (cursor.hasNext()) {
                result = cursor.next();
            }
        } catch (IOException ex) {
            if (log.isDebugEnabled()) {
                ex.printStackTrace();
            }
            throw new MessageException("jasper.loading.error", reportName, ex.getMessage());
        }
        return result;
    }

    @Override
    public JasperSource add(String reportName, MultipartFile jrXml, MultipartFile jasper) {
        try {
            JasperSource reportDetails = findByName(reportName);
            if (reportDetails == null) {
                reportDetails = new JasperSource();
                reportDetails.setReportName(reportName);
            } else {
                deleteJasperReport(getJasperClassPath(), reportName);
                log.info(localeMessage.getMessage("jasper.deleted", reportName));
            }
            Path classesPath = getJasperClassPath();
            Path jrXmlPath = classesPath.resolve(reportName + ".jrxml");
            Path jasperPath = classesPath.resolve(reportName + ".jasper");
            jrXml.transferTo(jrXmlPath);
            jasper.transferTo(jasperPath);
            reportDetails.setUpdatedOn(LocalDate.now());
            reportDetails.setJasperClassModified(LocalDate.now());
            if (reportDetails.isNew()) {
                arangoOperations.insert(reportDetails);
            } else {
                arangoOperations.update(reportDetails.getId(), reportDetails);
            }
            log.info(localeMessage.getMessage("jasper.added", reportName));
            return reportDetails;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MessageException("jasper.add.error", reportName, ex.getMessage());
        }
    }

    @Override
    public JasperSource update(JasperSource target) {
        arangoOperations.update(target.getId(), target);
        return target;
    }

    @Override
    public void delete(JasperSource reportDetails) {
        deleteJasperReport(getJasperClassPath(), reportDetails.getReportName());
        arangoOperations.delete(reportDetails.getId(), JasperSource.class);
    }

    @Override
    public JasperReport getJasperReport(String reportName) {
        if (reportName == null) {
            return null;
        }
        if (jasperReportMap == null) {
            initialize();
        }
        JasperReport jasperReport;
        if (!jasperReportMap.containsKey(reportName)) {
            try {
                jasperReport = loadJasperReport(getJasperClassPath(), reportName);
                jasperReportMap.put(reportName, jasperReport);
                return jasperReport;
            } catch (JRException ex) {
                ex.printStackTrace();
                throw new MessageException("jasper.loading.error", reportName, ex.getMessage());
            }
        }
        return jasperReportMap.get(reportName);
    }

    @Override
    public JsonObject getJasperDataFormat(JasperSource source) {
        if (jasperDataFormatMap == null) {
            initialize();
        }
        if (!jasperDataFormatMap.containsKey(source.getReportName())) {
            if (source.getDataFormat() != null) {
                try (JsonReader parser = Json.createReader(new StringReader(source.getDataFormat()))) {
                    JsonObject dataFormat = parser.readObject();
                    jasperDataFormatMap.put(source.getReportName(), dataFormat);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new MessageException("jasper.data_format.error", ex.getMessage());
                }
            }
        }
        if (jasperDataFormatMap.containsKey(source.getReportName())) {
            return jasperDataFormatMap.get(source.getReportName());
        }
        throw new MessageException("jasper.data_format.error");
    }

    public Path getJasperSourceFolder() {
        Path path;
        if (jasperSourceFolder == null || jasperSourceFolder.isBlank()) {
            jasperSourceFolder = System.getProperty("user.dir");
            path = Paths.get(jasperSourceFolder, "Jasper");
        } else {
            path = Paths.get(jasperSourceFolder);
        }
        return path;
    }

    @Override
    public JasperSource insert(JasperSource jasperSource) {
        arangoOperations.insert(jasperSource);
        return jasperSource;
    }

    private Path getJasperClassPath() {
        if (jasperSourcePath == null) {
            initialize();
            log.info(localeMessage.getMessage("jasper.class_path", jasperSourcePath));
        }
        return jasperSourcePath;
    }
}
