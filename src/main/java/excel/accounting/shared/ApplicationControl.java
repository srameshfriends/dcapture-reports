package excel.accounting.shared;

import com.google.gson.Gson;
import excel.accounting.db.DataProcessor;
import excel.accounting.db.AbstractDao;
import excel.accounting.db.TableReferenceFactory;
import excel.accounting.model.ApplicationConfig;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    private String userName, userCode;
    private Map<String, Object> beanMap;
    private TextField messagePanel;
    private TableReferenceFactory foreignKeyConstraint;

    private ApplicationControl() {
    }

    private void initialize() throws Exception {
        final Path configPath = FileHelper.getClassPath("config/application.json");
        if (configPath == null) {
            throw new NullPointerException("Application config json is missing");
        }
        foreignKeyConstraint = TableReferenceFactory.instance();
        Gson gson = new Gson();
        applicationConfig = gson.fromJson(new FileReader(configPath.toFile()), ApplicationConfig.class);
        beanMap = new HashMap<>();
        startDatabase();
    }

    public static ApplicationControl instance() throws Exception {
        if (applicationControl == null) {
            applicationControl = new ApplicationControl();
            applicationControl.initialize();
        }
        return applicationControl;
    }

    public boolean isAuthenticated() {
        return userCode != null;
    }

    public void setAuthenticated(String code, String name) {
        userCode = code;
        userName = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setMessagePanel(TextField field) {
        messagePanel = field;
    }

    public void setMessage(String message) {
        messagePanel.setText(message);
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
        if (!applicationConfig.isDevelopmentMode()) {
            DataProcessor.main();
        }
        dataProcessor = new DataProcessor();
        dataProcessor.startDatabase(applicationConfig);
    }

    public void addBean(String name, Object service) {
        if (service instanceof HasAppsControl) {
            ((HasAppsControl) service).setApplicationControl(this);
        }
        beanMap.put(name, service);
    }

    public Object getBean(String name) {
        return beanMap.get(name);
    }

    public void close() {
        dataProcessor.close();
    }
}
