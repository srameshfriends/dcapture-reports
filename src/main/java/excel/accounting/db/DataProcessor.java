package excel.accounting.db;

import excel.accounting.model.ApplicationConfig;
import excel.accounting.shared.FileHelper;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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

    private Connection getConnection() throws SQLException {
        return jdbcConnectionPool.getConnection();
    }

    private void addParameter(PreparedStatement statement, Map<Integer, Object> parameterMap) throws SQLException {
        for (Map.Entry<Integer, Object> entry : parameterMap.entrySet()) {
            Object parameter = entry.getValue();
            if (parameter == null) {
                statement.setString(entry.getKey(), null);
            } else if (parameter instanceof String) {
                statement.setString(entry.getKey(), (String) parameter);
            } else if (parameter instanceof Integer) {
                statement.setInt(entry.getKey(), (Integer) parameter);
            } else if (parameter instanceof BigDecimal) {
                statement.setBigDecimal(entry.getKey(), (BigDecimal) parameter);
            } else if (parameter instanceof Boolean) {
                statement.setBoolean(entry.getKey(), (Boolean) parameter);
            } else {
                DataType dataType = (DataType) parameter;
                if (DataType.DateType.equals(dataType)) {
                    statement.setDate(entry.getKey(), null);
                } else if (DataType.IntegerType.equals(dataType)) {
                    statement.setInt(entry.getKey(), 0);
                } else if (DataType.DoubleType.equals(dataType)) {
                    statement.setDouble(entry.getKey(), 0);
                } else if (DataType.BigDecimalType.equals(dataType)) {
                    statement.setBigDecimal(entry.getKey(), BigDecimal.ZERO);
                } else if (DataType.StringType.equals(dataType)) {
                    statement.setString(entry.getKey(), null);
                } else if (DataType.BooleanType.equals(dataType)) {
                    statement.setBoolean(entry.getKey(), false);
                }
            }
        }
    }

    public QueryBuilder getQueryBuilder(String fileName, String queryName) {
        String queryTemplate = null;
        Map<String, String> queryTemplateMap = namedQueryMap.get(fileName);
        if (queryTemplateMap != null) {
            queryTemplate = queryTemplateMap.get(queryName);
        }
        return queryTemplate == null ? new QueryBuilder(queryName) : new QueryBuilder(queryName, queryTemplate);
    }

    public Object findObject(QueryBuilder query) {
        logger.debug(query.getQuery());
        Object value = null;
        try {
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query.getQuery());
            if (rs.next()) {
                value = rs.getObject(1);
            }
            close(rs, stmt, con);
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return value;
    }

    public List<Object[]> findObjects(QueryBuilder builder) {
        logger.info(builder.getQuery());
        try {
            List<Object[]> resultList = new ArrayList<>();
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(builder.getQuery());
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] result = new Object[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    result[col] = rs.getObject(col + 1);
                }
                resultList.add(result);
            }
            close(rs, stmt, con);
            return resultList;
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public <T> List<T> findRowDataList(QueryBuilder builder, RowTypeConverter<T> rowTypeConverter) {
        List<T> dataList = new ArrayList<>();
        List<Object[]> objList = findObjects(builder);
        for (Object[] obj : objList) {
            T rowData = rowTypeConverter.getRowType(builder, obj);
            if (rowData != null) {
                dataList.add(rowData);
            }
        }
        return dataList;
    }

    public int insert(QueryBuilder query) {
        logger.debug(query.getQuery());
        try {
            Connection con = getConnection();
            PreparedStatement statement = con.prepareStatement(query.getQuery());
            addParameter(statement, query.getParameters());
            int result = statement.executeUpdate();
            close(statement, con);
            return result;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int update(QueryBuilder queryBuilder) {
        logger.debug(queryBuilder.getQuery());
        try {
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            int result = stmt.executeUpdate(queryBuilder.getQuery());
            close(stmt, con);
            return result;
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public int delete(QueryBuilder queryBuilder) {
        logger.debug(queryBuilder.getQuery());
        try {
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            int result = stmt.executeUpdate(queryBuilder.getQuery());
            close(stmt, con);
            return result;
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    public int create(String query) {
        logger.debug(query);
        try {
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            int result = stmt.executeUpdate(query);
            close(stmt, con);
            return result;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void close(Statement statement, Connection connection) {
        try {
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void close(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void executeTableForwardQuery() throws Exception {
        List<String> queryList = new ArrayList<>();
        for (Map<String, String> queryMap : namedQueryMap.values()) {
            queryList.addAll(queryMap.values().stream().filter("create table"::equals).collect(Collectors.toList()));
        }
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + getSchema() + ";");
        for (String createQuery : queryList) {
            logger.info("Table : " + createQuery);
            statement.executeUpdate(createQuery);
        }
        statement.close();
        connection.close();
    }

    private void addNamedQueries() throws Exception {
        Path path = FileHelper.getClassPath("");
        File dir = path == null ? null : path.toFile();
        List<File> sqlFileList = dir == null ? null : FileHelper.getFilesInDirectory(dir, "sql");
        System.out.println(sqlFileList == null ? "file not found " : sqlFileList.size());
        if (sqlFileList != null) {
            for (File sqlFile : sqlFileList) {
                addNamedQueries(sqlFile);
            }
        }
    }

    private void addNamedQueries(File sqlFile) throws Exception {
        final String fileName = FileHelper.getNameWithoutExtension(sqlFile);
        Map<String, String> queryTemplateMap = new HashMap<>();
        namedQueryMap.put(fileName, queryTemplateMap);
        //
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
    }

    public static void main(String... args) throws Exception {
        Server.createTcpServer().start();
        Server.createWebServer().start();
    }
}
