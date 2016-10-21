package excel.accounting.db;

import excel.accounting.model.ApplicationConfig;
import excel.accounting.shared.FileHelper;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

/**
 * Data Processor
 */
public class DataProcessor {
    private static final Logger logger = Logger.getLogger(DataProcessor.class);
    private JdbcConnectionPool jdbcConnectionPool;
    private Map<String, Map<String, String>> namedQueryMap;

    public void startDatabase(ApplicationConfig config) throws Exception {
        jdbcConnectionPool = JdbcConnectionPool.create(config.getDatabaseUrl(), config.getDatabaseUser(),
                config.getDatabasePassword());
        namedQueryMap = new HashMap<>();
        addNamedQueries();
        executeTableForwardQuery();
    }

    private String getSchema() {
        return "entity";
    }

    Connection getConnection() throws SQLException {
        return jdbcConnectionPool.getConnection();
    }

    public QueryBuilder getQueryBuilder(String fileName, String queryName) {
        Map<String, String> queryTemplateMap = namedQueryMap.get(fileName);
        if (queryTemplateMap == null) {
            throw new NullPointerException("query sql file not found " + fileName);
        }
        String queryTemplate = queryTemplateMap.get(queryName);
        if (queryTemplate == null) {
            throw new NullPointerException(fileName + " : Query template not found " + queryName);
        }
        return new QueryBuilder(queryName, queryTemplate);
    }

    private void executeTableForwardQuery() throws Exception {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + getSchema() + ";");
        for (Map<String, String> queryMap : namedQueryMap.values()) {
            for (String query : queryMap.values()) {
                if (query.startsWith("create table")) {
                    logger.debug("Table : " + query);
                    statement.executeUpdate(query);
                }
            }
        }
        statement.close();
        connection.close();
    }

    private void addNamedQueries() throws Exception {
        Path path = FileHelper.getClassPath("sql");
        File dir = path == null ? null : path.toFile();
        List<File> sqlFileList = dir == null ? null : FileHelper.getFilesInDirectory(dir, "sql");
        if (sqlFileList != null) {
            for (File sqlFile : sqlFileList) {
                addNamedQueries(sqlFile);
            }
        }
    }

    private void addNamedQueries(File sqlFile) throws Exception {
        final String fileName = FileHelper.getNameWithoutExtension(sqlFile);
        Map<String, String> queryTemplateMap = new HashMap<>();
        List<String> sqlList = FileHelper.readAllLines(sqlFile);
        String name = "", query;
        StringBuilder builder = new StringBuilder();
        for (String sql : sqlList) {
            if (sql.trim().startsWith("--")) {
                query = builder.toString();
                if (!name.isEmpty() && !query.isEmpty()) {
                    queryTemplateMap.put(name, query.toLowerCase().trim());
                }
                name = sql.trim().replace("--", "");
                builder = new StringBuilder();
            } else {
                builder.append(sql);
            }
        }
        namedQueryMap.put(fileName, queryTemplateMap);
    }

    public void close() {
        jdbcConnectionPool.dispose();
    }

    public static void main(String... args) throws Exception {
        Server.createTcpServer().start();
        Server.createWebServer().start();
    }
}
