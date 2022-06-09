package dcapture.reports.jasper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class JasperServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(JasperServlet.class);
    private static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 256 * 1024;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Path tempReportFolder;
    private String contextPath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ReportUtils.setJRRootFolder(config.getServletContext());
        String conPath = config.getServletContext().getInitParameter("context_path");
        contextPath = conPath == null ? "/dcapture-reports" : conPath;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String serviceName = getServiceName(request.getPathInfo());
        if ("download".equalsIgnoreCase(serviceName)) {
            downloadReport(request, response);
        } else if ("loadAll".equalsIgnoreCase(serviceName)) {
            sendReportModels(response, ReportUtils.getJasperReports());
        } else if ("refresh".equalsIgnoreCase(serviceName)) {
            ReportUtils.setJRRootFolder(request.getServletContext());
            String conPath = request.getServletContext().getInitParameter("context_path");
            contextPath = conPath == null ? "/dcapture-reports" : conPath;
            logger.info("Jasper report configuration reloaded.");
            sendJsonMessage(response, "Jasper report configuration reloaded.");
        } else {
            sendError(response, "Requested service not available.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String serviceName = getServiceName(req.getPathInfo());
        if ("generate/link".equalsIgnoreCase(serviceName)) {
            generateLink(req, resp);
        } else if ("generate".equalsIgnoreCase(serviceName)) {
            generateReport(req, resp);
        } else if ("loadAll".equalsIgnoreCase(serviceName)) {
            sendReportModels(resp, ReportUtils.getJasperReports());
        } else if ("upload".equalsIgnoreCase(serviceName)) {
            addJasperJrXmlFiles(req, resp);
        } else if ("save".equalsIgnoreCase(serviceName)) {
            saveDataFormat(req, resp);
        } else if ("delete".equalsIgnoreCase(serviceName)) {
            deleteJasperJrXmlFiles(req, resp);
        } else {
            sendError(resp, serviceName + ", (POST) Requested service not found.");
        }
    }

    private String getServiceName(String pathInfo) {
        String servicePath = pathInfo == null ? "" : pathInfo.trim();
        if (servicePath.startsWith("/")) {
            servicePath = servicePath.substring(1);
        }
        if (servicePath.endsWith("/")) {
            servicePath = servicePath.substring(0, servicePath.length() - 1);
        }
        return servicePath;
    }

    private void addJasperJrXmlFiles(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reportName = request.getParameter("report_name");
        if (reportName == null || reportName.trim().isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Report name should not be empty.");
            return;
        }
        Part jrXmlPart = null, jasperPart = null;
        for (Part part : request.getParts()) {
            String fileName = ReportUtils.getFileName(part);
            if (ReportUtils.isAcceptedType(part.getContentType(), fileName)) {
                if (ReportUtils.isJrXmlType(fileName)) {
                    jrXmlPart = part;
                } else if (ReportUtils.isJasperType(fileName)) {
                    jasperPart = part;
                }
            }
        }
        if (jrXmlPart != null && jasperPart != null) {
            String message = ReportUtils.addJasperReport(reportName, jrXmlPart, jasperPart);
            if (message == null) {
                logger.info(reportName + " added into the jasper report service.");
                response.sendRedirect("/index.html?message=" + reportName + " added into the jasper report service.");
            } else {
                logger.info(reportName + " : " + message);
                response.sendRedirect("/index.html?message=" + reportName + " : " + message);
            }
        } else {
            logger.info("ERROR : jrXml Or jasper not valid to add at report service (" + reportName + ").");
            response.sendRedirect(contextPath
                    + "/index.html?message=ERROR : jrXml Or jasper not valid to add at report service ("
                    + reportName + ").");
        }
    }

    private void saveDataFormat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String reportName = request.getParameter("report_name");
        if (reportName == null || reportName.trim().isBlank()) {
            sendError(response, "Report name should not be empty.");
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode parameters = objectMapper.readValue(request.getReader(), ObjectNode.class);
        String message = ReportUtils.saveJasperReportFormat(reportName, parameters.toString());
        if (message == null) {
            sendJsonMessage(response, "Jasper report data format saved (" + reportName + ").");
            logger.info("Jasper report data format saved (" + reportName + ").");
        } else {
            sendError(response, message);
        }
    }

    private void deleteJasperJrXmlFiles(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String reportName = request.getParameter("report_name");
        if (reportName == null || reportName.trim().isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Report name should not be empty.");
        } else {
            String message = ReportUtils.deleteJasperReport(reportName);
            if (message == null) {
                sendJsonMessage(response, "Jasper report deleted (" + reportName + ")");
                logger.info("Jasper report deleted (" + reportName + ")");
            } else {
                sendError(response, message);
            }
        }
    }

    private void generateLink(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String reportName = request.getParameter("report_name");
        JasperReport jasperReport = ReportUtils.getJasperReport(reportName);
        if (jasperReport == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ERROR : Report not found (" + reportName + ")");
            return;
        }
        try {
            JsonJRDataSource dataSource = new JsonJRDataSource(request, ReportUtils.getDataFormat(reportName));
            JasperPrint jasperPrint
                    = JasperFillManager.fillReport(jasperReport, dataSource.getParameters(), dataSource);
            String link = saveRandomPdfReport(jasperPrint, reportName);
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            String kk = getDownloadUrl(request, link);
            objectNode.put("link", kk);
            sendJson(response, objectNode);
        } catch (JRException ex) {
            ex.printStackTrace();
            sendError(response, "ERROR : To generate report, " + ex.getMessage());
        }
    }

    private void downloadReport(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String link = request.getParameter("link");
        Path reportPath = getTempReportFolder();
        reportPath = link == null ? null : reportPath.resolve(link);
        if (reportPath == null || !Files.exists(reportPath)) {
            sendError(response, "Report not found.");
            return;
        }
        response.setContentType(getContentType(link));
        response.setContentLength(((int) Files.size(reportPath)));
        response.setHeader("Content-Encoding", ENCODING);
        response.setHeader("Content-disposition", "inline; filename=" + link);
        response.setStatus(HttpServletResponse.SC_OK);
        try (FileInputStream fileInputStream = new FileInputStream(reportPath.toFile())) {
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                FileChannel channel = fileInputStream.getChannel();
                byte[] buffer = new byte[BUFFER_SIZE];
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                for (int length; (length = channel.read(byteBuffer)) != -1; ) {
                    outputStream.write(buffer, 0, length);
                    byteBuffer.clear();
                }
                outputStream.flush();
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        try {
            if (Files.exists(reportPath)) {
                Files.delete(reportPath);
            }
        } catch (IOException ex) {
            logger.info("ERROR: Jasper Report Temp files delete error : " + ex.getMessage());
        }

    }

    public void generateReport(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String reportName = request.getParameter("report_name");
        JasperReport jasperReport = ReportUtils.getJasperReport(reportName);
        if (jasperReport == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ERROR : Report not found (" + reportName + ")");
            return;
        }
        String error = null;
        try {
            JsonJRDataSource dataSource = new JsonJRDataSource(request, ReportUtils.getDataFormat(reportName));
            JasperPrint jasperPrint
                    = JasperFillManager.fillReport(jasperReport, dataSource.getParameters(), dataSource);
            sendPdfResponse(response, jasperPrint, reportName);
        } catch (JRException ex) {
            ex.printStackTrace();
            error = "ERROR : To generate report, " + ex.getMessage();
        }
        if (error != null) {
            sendError(response, error);
        } else {
            sendJsonMessage(response, "Your request send to the printer.");
        }
    }

    private void sendError(HttpServletResponse response, String content) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, content);
    }

    private void sendJsonMessage(HttpServletResponse response, String message) {
        response.setContentType("application/json");
        response.setCharacterEncoding(ENCODING);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("status", HttpServletResponse.SC_OK);
            objectNode.put("responseText", message == null ? "" : message);
            objectMapper.writeValue(response.getOutputStream(), objectNode);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void sendJson(HttpServletResponse response, ObjectNode objectNode) {
        response.setContentType("application/json");
        response.setCharacterEncoding(ENCODING);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getOutputStream(), objectNode);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void sendReportModels(HttpServletResponse response, List<ReportModel> reportModels) {
        response.setContentType("application/json");
        response.setCharacterEncoding(ENCODING);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getOutputStream(), reportModels);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void sendPdfResponse(HttpServletResponse response, JasperPrint jasperPrint, String reportName) {
        String name = timeFormat.format(new Date());
        name = reportName + "-" + name.replaceAll("-", "");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            response.setContentType("application/pdf");
            response.setHeader("Content-disposition", "inline; filename=" + name + ".pdf");
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
    }

    private String saveRandomPdfReport(JasperPrint jasperPrint, String reportName) {
        String name = reportName + dateFormat.format(new Date());
        name = name + UUID.randomUUID();
        name = name.replaceAll("-", "") + ".pdf";
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

    private String getDownloadUrl(HttpServletRequest request, String fileName) {
        return request.getRequestURL().toString().replace("generate/link", "download?link=" + fileName);
    }

    private Path getTempReportFolder() {
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

    private String getContentType(String link) {
        if (link.contains("pdf")) {
            return "application/pdf";
        } else if (link.contains("csv")) {
            return "application/csv";
        }
        return "html/text";
    }
}
