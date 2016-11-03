package excel.accounting.shared;

import com.google.gson.Gson;
import excel.accounting.db.DataProcessor;
import excel.accounting.db.AbstractDao;
import excel.accounting.db.TableReferenceFactory;
import excel.accounting.model.ApplicationConfig;
import javafx.scene.control.TextArea;

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
    private Map<String, Object> serviceMap;
    private Map<String, AbstractDao> relationMap;
    private TextArea messagePanel;
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
        serviceMap = new HashMap<>();
        relationMap = new HashMap<>();
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

    public void setMessagePanel(TextArea textArea) {
        messagePanel = textArea;
    }

    public void setMessage(String message) {
        messagePanel.setText(message);
    }

    public void appendMessage(String message) {
        if (messagePanel.getText().length() == 0) {
            messagePanel.setText(message);
        } else {
            messagePanel.selectEnd();
            messagePanel.insertText(messagePanel.getText().length(), " \n " + message);
        }
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

    public void addService(String name, Object service) {
        if (service instanceof HasAppsControl) {
            ((HasAppsControl) service).setApplicationControl(this);
        }
        serviceMap.put(name, service);
    }

    public void addDao(String name, AbstractDao relation) {
        relation.setApplicationControl(this);
        relationMap.put(name, relation);
    }

    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }

    public Object getDao(String relationName) {
        return relationMap.get(relationName);
    }

    public void close() {
        dataProcessor.close();
    }
}
