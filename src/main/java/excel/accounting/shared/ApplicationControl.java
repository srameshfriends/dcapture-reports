package excel.accounting.shared;

import com.google.gson.Gson;
import excel.accounting.db.DataProcessor;
import excel.accounting.model.ApplicationConfig;

import java.io.FileReader;
import java.nio.file.Path;

/**
 * Application Control
 */
public class ApplicationControl {
    private static ApplicationControl applicationControl;
    private ApplicationConfig applicationConfig;
    private DataProcessor dataProcessor;

    private ApplicationControl() {
    }

    private void initialize() throws Exception {
        final Path configPath = FileHelper.getClassPathLocation("config/application.json");
        if (configPath == null) {
            throw new NullPointerException("Application config json is missing");
        }
        Gson gson = new Gson();
        applicationConfig = gson.fromJson(new FileReader(configPath.toFile()), ApplicationConfig.class);
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

    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public String getName() {
        return applicationConfig.getName();
    }

    private void startDatabase() throws Exception {
        if(!applicationConfig.isDevelopmentMode()) {
            DataProcessor.main();
        }
        dataProcessor = new DataProcessor();
        dataProcessor.startDatabase(applicationConfig);
    }
}
