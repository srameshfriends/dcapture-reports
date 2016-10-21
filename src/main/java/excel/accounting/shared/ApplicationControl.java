package excel.accounting.shared;

import com.google.gson.Gson;
import excel.accounting.db.DataProcessor;
import excel.accounting.db.HasDataProcessor;
import excel.accounting.model.ApplicationConfig;
import org.apache.xmlbeans.impl.common.ConcurrentReaderHashMap;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Control
 */
public class ApplicationControl {
    private static ApplicationControl applicationControl;
    private ApplicationConfig applicationConfig;
    private DataProcessor dataProcessor;
    private Map<String, Object> serviceMap;

    private ApplicationControl() {
    }

    private void initialize() throws Exception {
        final Path configPath = FileHelper.getClassPath("config/application.json");
        if (configPath == null) {
            throw new NullPointerException("Application config json is missing");
        }
        Gson gson = new Gson();
        applicationConfig = gson.fromJson(new FileReader(configPath.toFile()), ApplicationConfig.class);
        serviceMap = new HashMap<>();
        startDatabase();
    }

    public static ApplicationControl instance() throws Exception {
        if (applicationControl == null) {
            applicationControl = new ApplicationControl();
            applicationControl.initialize();
        }
        return applicationControl;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public String getName() {
        return applicationConfig.getName();
    }

    private void startDatabase() throws Exception {
        if (!applicationConfig.isDevelopmentMode()) {
            DataProcessor.main();
        }
        dataProcessor = new DataProcessor();
        dataProcessor.startDatabase(applicationConfig);
    }

    public void addService(String name, Object service) {
        if (service instanceof HasDataProcessor) {
            ((HasDataProcessor) service).setDataProcessor(getDataProcessor());
        }
        serviceMap.put(name, service);
    }

    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }

    public void close() {
        dataProcessor.close();
    }
}
