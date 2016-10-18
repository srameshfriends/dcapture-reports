package excel.accounting.db;

import excel.accounting.model.ApplicationConfig;
import excel.accounting.shared.RowDataProvider;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * Data Processor
 */
public class DataProcessor {
    private static final Logger logger = Logger.getLogger(DataProcessor.class);
    private JdbcConnectionPool jdbcConnectionPool;

    public void startDatabase(ApplicationConfig config) throws Exception {
        jdbcConnectionPool = JdbcConnectionPool.create(config.getDatabaseUrl(), config.getDatabaseUser(),
                config.getDatabasePassword());

    }

    private Connection getConnection() throws SQLException {
        return jdbcConnectionPool.getConnection();
    }

    private void addParameter(PreparedStatement statement, List<Object> parameterList) throws SQLException {
        int index = 1;
        for (Object parameter : parameterList) {
            if (parameter == null) {
                index += 1;
                continue;
            }
            if (parameter instanceof String) {
                statement.setString(index, (String) parameter);
            } else if (parameter instanceof Integer) {
                statement.setInt(index, (Integer) parameter);
            } else if (parameter instanceof BigDecimal) {
                statement.setBigDecimal(index, (BigDecimal) parameter);
            } else if (parameter instanceof Boolean) {
                statement.setBoolean(index, (Boolean) parameter);
            }
            index += 1;
        }
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

    public List<Object[]> findObjects(QueryBuilder query) {
        logger.debug(query.getQuery());
        try {
            List<Object[]> resultList = new ArrayList<>();
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query.getQuery());
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

    public <T> List<T> findRowDataList(QueryBuilder builder, RowDataProvider<T> rowDataProvider) {
        List<T> dataList = new ArrayList<>();
        List<Object[]> objList = findObjects(builder);
        for (Object[] obj : objList) {
            T rowData = rowDataProvider.getRowData(builder.getName(), obj);
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

    public String createSchema() {
        return "CREATE SCHEMA IF NOT EXISTS entity";
    }

    public static void main(String... args) throws Exception {
        Server.createTcpServer().start();
        Server.createWebServer().start();
    }
}
