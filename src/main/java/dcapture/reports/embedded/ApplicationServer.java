package dcapture.reports.embedded;

import dcapture.reports.jasper.JasperServlet;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationServer {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServer.class);
    private static final int SERVICE_PORT = 8181;
    private static final String RESOURCE_PATH = "C:\\workspace\\dcapture-reports\\webapps";

    public static void main(String... args) {
        try {
            ApplicationServer server = new ApplicationServer();
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addInitParam(ServletContextHandler context) {
        context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        context.setInitParameter("jasper_reports_path", "C:\\workspace\\jasper-reports");
        context.setInitParameter("host_url", "http://localhost:8181");
        context.setInitParameter("context_path", "/dcapture-reports");
        context.setInitParameter("mode", "debug");
    }

    private void start() throws Exception {
        Server server = new Server(SERVICE_PORT);
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);

        servletContext.setBaseResource(Resource.newResource(Paths.get(RESOURCE_PATH)));
        servletContext.setContextPath("/dcapture-reports/");
        servletContext.insertHandler(new GzipHandler());
        servletContext.setWelcomeFiles(new String[]{"index.html"});
        //
        ServletHolder defaultHolder = new ServletHolder(new DefaultServlet());
        servletContext.addServlet(defaultHolder, "/*");
        ServletHolder refreshHolder = new ServletHolder(new JasperServlet());
        refreshHolder.setInitOrder(1);
        refreshHolder.getRegistration().setMultipartConfig(getMultipartConfig());
        servletContext.addServlet(refreshHolder, "/jasper/*");
        addInitParam(servletContext);
        server.setHandler(servletContext);
        servletContext.setAttribute(Server.class.getName(), server);
        logger.info("Reports service port  :" + SERVICE_PORT);
        server.start();
        server.join();
    }

    private MultipartConfigElement getMultipartConfig() { // 5 MB , 20 MB, 0
        Path path = Paths.get(System.getProperty("java.io.tmpdir"), "dcapture", "multipart");
        return new MultipartConfigElement(path.toString()); // 5242880L, 20971520L, 0
    }
}
