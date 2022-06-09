package dcapture.reports.jasper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ReportUtils {
    private static final Logger logger = LoggerFactory.getLogger(ReportUtils.class);
    private static Path jasperRootFolder = null;
    private static Map<String, JasperReport> jasperReportMap;
    private static Map<String, ReportModel> reportModelMap;

    private static void initJasperReport(final Path rootFolder) {
        try {
            if (!Files.exists(rootFolder)) {
                Files.createDirectories(rootFolder);
            }
            Path configFolder = rootFolder.resolve("config"), classesFolder = rootFolder.resolve("classes");
            if (!Files.exists(configFolder)) {
                Files.createDirectories(configFolder);
            }
            if (!Files.exists(classesFolder)) {
                Files.createDirectories(classesFolder);
            }
            Path jasperReportsPath = configFolder.resolve("jasper-reports.json");
            if (!Files.exists(jasperReportsPath)) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(jasperReportsPath.toFile(), new ArrayList<>());
            }
            jasperRootFolder = rootFolder;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Path getJRRootFolder() {
        if (jasperRootFolder == null) {
            try {
                Path temp = Paths.get(System.getProperty("java.io.tmpdir"));
                temp = temp.resolve("dcapture");
                temp = temp.resolve("jasper-reports");
                if (!Files.exists(temp)) {
                    Files.createDirectories(temp);
                }
                initJasperReport(temp);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return jasperRootFolder;
    }

    public static void setJRRootFolder(ServletContext servletContext) {
        String jasperReportPath = servletContext.getInitParameter("jasper_reports_path");
        initJasperReport(Paths.get(jasperReportPath));
        reloadJasperReports();
        reloadReportModels();
    }

    public static void setJRRootFolder(Path folder) {
        initJasperReport(folder);
    }

    public static Path getJRConfigPath() {
        Path path = getJRRootFolder().resolve("config");
        path = path.resolve("jasper-reports.json");
        return path;
    }

    public static Path getJRClassesPath() {
        return getJRRootFolder().resolve("classes");
    }

    public static boolean isAcceptedType(String contentType, String fileName) {
        if (fileName == null || contentType == null || contentType.isBlank()) {
            return false;
        }
        return fileName.contains(".jrxml") || fileName.contains(".jasper");
    }

    public static boolean isJrXmlType(String fileName) {
        return fileName.contains(".jrxml");
    }

    public static boolean isJasperType(String fileName) {
        return fileName.contains(".jasper");
    }

    public static synchronized List<ReportModel> getJasperReports() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return Arrays.asList(mapper.readValue(getJRConfigPath().toFile(), ReportModel[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static synchronized void reloadJasperReports() {
        Path folder = getJRClassesPath();
        List<ReportModel> reportModelList = getJasperReports();
        Map<String, JasperReport> map = new HashMap<>();
        for (ReportModel model : reportModelList) {
            Path jasperFile = folder.resolve(model.getReportName() + ".jasper");
            if (Files.exists(jasperFile)) {
                try {
                    JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(jasperFile.toString());
                    map.put(model.getReportName(), jasperReport);
                } catch (JRException ex) {
                    logger.error("ERROR : " + model.getReportName() + ", Jasper report loading (" + jasperFile + ")");
                }
            }
        }
        jasperReportMap = Collections.unmodifiableMap(map);
    }

    private static void reloadReportModels() {
        List<ReportModel> reportModels = getJasperReports();
        Map<String, ReportModel> map = new HashMap<>();
        reportModels.forEach(model -> map.put(model.getReportName(), model));
        reportModelMap = Collections.unmodifiableMap(map);
    }

    public static String addJasperReport(String reportName, Part jrXml, Part jasper) {
        String error = addJasperReportName(reportName);
        try {
            Path classesPath = getJRClassesPath();
            Path jrXmlPath = classesPath.resolve(reportName + ".jrxml");
            Path jasperPath = classesPath.resolve(reportName + ".jasper");
            jrXml.write(jrXmlPath.toString());
            jasper.write(jasperPath.toString());
            reloadJasperReports();
            reloadReportModels();
        } catch (IOException ex) {
            ex.printStackTrace();
            error = error == null ? ex.getMessage() : error + ", " + ex.getMessage();
        }
        return error;
    }

    public static synchronized String deleteJasperReport(String reportName) {
        String error = null;
        try {
            error = deleteJasperReportName(reportName);
            Path classPath = getJRClassesPath();
            Path jrXml = classPath.resolve(reportName + ".jrxml");
            if (Files.exists(jrXml)) {
                Files.delete(jrXml);
            }
            Path jasper = classPath.resolve(reportName + ".jasper");
            if (Files.exists(jasper)) {
                Files.delete(jasper);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (error == null) {
                error = ex.getMessage();
            } else {
                error = error + ", " + ex.getMessage();
            }
        }
        return error;
    }

    public static String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf("=") + 2, content.length() - 1);
            }
        }
        return null;
    }

    public static String saveJasperReportFormat(String reportName, String dataFormat) {
        String error = null;
        try {
            boolean isUpdated = false;
            List<ReportModel> modelList = getJasperReports();
            List<ReportModel> resultList = new ArrayList<>();
            for (ReportModel model : modelList) {
                if (reportName.equals(model.getReportName())) {
                    model.setDataFormat(dataFormat);
                    isUpdated = true;
                }
                resultList.add(model);
            }
            if (isUpdated) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(getJRConfigPath().toFile(), resultList);
            } else {
                error = "Jasper report not found to save data format.";
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            error = ex.getMessage();
        }
        return error;
    }

    private static String addJasperReportName(String reportName) {
        String error = null;
        try {
            boolean isUpdated = false;
            List<ReportModel> modelList = getJasperReports();
            List<ReportModel> resultList = new ArrayList<>();
            for (ReportModel model : modelList) {
                if (reportName.equals(model.getReportName())) {
                    model.setDataFormat("{}");
                    isUpdated = true;
                }
                resultList.add(model);
            }
            if (!isUpdated) {
                resultList.add(new ReportModel(reportName, "{}"));
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(getJRConfigPath().toFile(), resultList);
        } catch (IOException ex) {
            ex.printStackTrace();
            error = ex.getMessage();
        }
        return error;
    }

    private static String deleteJasperReportName(String reportName) {
        String error = null;
        try {
            List<ReportModel> modelList = getJasperReports();
            List<ReportModel> resultList = new ArrayList<>();
            for (ReportModel model : modelList) {
                if (!reportName.equals(model.getReportName())) {
                    resultList.add(model);
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(getJRConfigPath().toFile(), resultList);
        } catch (IOException ex) {
            ex.printStackTrace();
            error = ex.getMessage();
        }
        return error;
    }

    public static JasperReport getJasperReport(String reportName) {
        if (jasperReportMap == null) {
            reloadJasperReports();
            reloadReportModels();
        }
        return reportName == null ? null : jasperReportMap.get(reportName);
    }

    private static String getReportDataFormat(String reportName) {
        if (reportModelMap == null) {
            reloadReportModels();
        }
        return reportName == null || !reportModelMap.containsKey(reportName) ? null
                : reportModelMap.get(reportName).getDataFormat();
    }

    public static ObjectNode getDataFormat(String reportName) {
        ObjectMapper mapper = new ObjectMapper();
        String dataFormat = getReportDataFormat(reportName);
        if (dataFormat == null) {
            return mapper.createObjectNode();
        }
        try {
            return mapper.readValue(dataFormat, ObjectNode.class);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return mapper.createObjectNode();
    }
}
