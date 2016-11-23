package excel.accounting.shared;

import com.google.gson.Gson;
import excel.accounting.db.*;
import excel.accounting.model.ApplicationConfig;
import javafx.scene.control.TextField;
import org.h2.jdbcx.JdbcConnectionPool;

import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application Control
 */
public class ApplicationControl {
    private static ApplicationControl applicationControl;
    private ApplicationConfig applicationConfig;
    private DataProcessor dataProcessor;
    private SqlTableMap sqlTableMap;
    private SqlForwardTool sqlForwardTool;
    private JdbcConnectionPool connectionPool;
    private String userName, userCode;
    private Map<String, Object> beanMap;
    private TextField messagePanel;
    private Map<String, String> messageMap;

    private ApplicationControl() {
    }

    private void initialize() throws Exception {
        final Path configPath = FileHelper.getClassPath("config/application.json");
        if (configPath == null) {
            throw new NullPointerException("Application config json is missing");
        }
        Gson gson = new Gson();
        applicationConfig = gson.fromJson(new FileReader(configPath.toFile()), ApplicationConfig.class);
        beanMap = new HashMap<>();
        startDatabase();
        loadMessages();
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

    private void loadMessages() throws Exception {
        messageMap = new HashMap<>();
        final Path path = FileHelper.getClassPath("config/message.properties");
        if (path != null) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(path.toFile()));
            for (String name : properties.stringPropertyNames()) {
                messageMap.put(name, properties.getProperty(name));
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getMessage(String name) {
        return messageMap.get(name);
    }

    public String getMessage(String name, Object... params) {
        String msg = messageMap.get(name);
        if (params != null && msg != null) {
            return MessageFormat.format(msg, params);
        }
        return msg;
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

    DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public JdbcConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public SqlTableMap getSqlTableMap() {
        return sqlTableMap;
    }

    public String getName() {
        return applicationConfig.getName();
    }

    private void startDatabase() throws Exception {
        if (!applicationConfig.isDevelopmentMode()) {
            DataProcessor.main();
        }
        connectionPool = JdbcConnectionPool.create(applicationConfig.getDatabaseUrl(),
                applicationConfig.getDatabaseUser(), applicationConfig.getDatabasePassword());
        dataProcessor = new DataProcessor(connectionPool);
        dataProcessor.run();
        sqlTableMap = SqlFactory.createSqlTableMap("excel", getEntityPackages());
        sqlForwardTool = new H2ForwardTool();
        sqlForwardTool.setSqlTableMap(sqlTableMap);
        sqlForwardTool.setSqlEnumParser(new SqlEnumParserImpl());
        runForwardTool();
    }

    private void runForwardTool() {
        SqlQuery createSchema = sqlForwardTool.createSchemaQuery();
        List<SqlQuery> createTableList = sqlForwardTool.createTableQueries();
        List<SqlQuery> alterTableList = sqlForwardTool.alterTableQueries();
        SqlTransaction transaction = new SqlTransaction(connectionPool);
        transaction.add(createSchema);
        transaction.addAll(createTableList);
        transaction.addAll(alterTableList);
        SqlWriter response = new SqlWriter() {
            @Override
            public void onSqlUpdated(int pid) {
            }

            @Override
            public void onSqlError(int pid, SQLException ex) {
                ex.printStackTrace();
            }
        };
        transaction.setProcessId(1);
        transaction.setResponse(response);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(transaction);
    }

    public void addBean(String name, Object service) {
        if (service instanceof AbstractControl) {
            ((AbstractControl) service).setApplicationControl(this);
        }
        beanMap.put(name, service);
    }


    public Object getBean(String name) {
        return beanMap.get(name);
    }

    public void close() {
        dataProcessor.close();
    }

    public SqlForwardTool getSqlForwardTool() {
        return sqlForwardTool;
    }

    private String[] getEntityPackages() {
        return new String[]{"excel.accounting.entity"};
    }
}
