package excel.accounting.db;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * H2 Transaction
 */
public class H2Transaction implements SqlTransaction {
    private H2Processor processor;

    public H2Transaction(H2Processor processor) {
        this.processor = processor;
    }

    @Override
    public void executeBatch(SqlQuery... queries) throws SQLException {
        List<SqlQuery> queryList = new ArrayList<>();
        Collections.addAll(queryList, queries);
        executeBatch(queryList);
    }

    @Override
    public void executeCommit(SqlQuery... queries) throws SQLException {
        List<SqlQuery> queryList = new ArrayList<>();
        Collections.addAll(queryList, queries);
        executeCommit(queryList);
    }

    @Override
    public void insert(List<Object> dataList) throws SQLException {
        List<SqlQuery> queryList = new ArrayList<>();
        for (Object object : dataList) {
            queryList.add(processor.insertQuery(object));
        }
        executeCommit(queryList);
    }

    @Override
    public void update(List<Object> dataList) throws SQLException {
        List<SqlQuery> queryList = new ArrayList<>();
        for (Object object : dataList) {
            queryList.add(processor.updateQuery(object));
        }
        executeCommit(queryList);
    }

    @Override
    public void delete(List<Object> dataList) throws SQLException {
        List<SqlQuery> queryList = new ArrayList<>();
        for (Object object : dataList) {
            queryList.add(processor.deleteQuery(object));
        }
        executeCommit(queryList);
    }

    @Override
    public void executeBatch(List<SqlQuery> queries) throws SQLException {
        Connection connection = processor.getConnectionPool().getConnection();
        connection.setAutoCommit(false);
        try {
            SqlQuery sqlQuery = queries.get(0);
            PreparedStatement statement = connection.prepareStatement(sqlQuery.getQuery());
            for (SqlQuery sql : queries) {
                addParameter(statement, sql);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            close(statement);
            close(connection);
        } catch (SQLException ex) {
            rollback(connection);
            throw ex;
        }
    }

    @Override
    public void executeCommit(List<SqlQuery> queries) throws SQLException {
        Connection connection = null;
        try {
            connection = processor.getConnectionPool().getConnection();
            connection.setAutoCommit(false);
            for (SqlQuery sql : queries) {
                PreparedStatement statement = connection.prepareStatement(sql.getQuery());
                addParameter(statement, sql);
                statement.execute();
            }
            connection.commit();
            close(connection);
        } catch (SQLException ex) {
            rollback(connection);
            throw ex;
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            // ignore
        }
    }

    private void close(Statement statement) {
        try {
            statement.close();
        } catch (Exception ex) {
            // ignore
        }
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (Exception ex) {
            // ignore
        }
    }

    private void addParameter(PreparedStatement statement, List<Object> objects) throws SQLException {
        int index = 1;
        for (Object parameter : objects) {
            if (parameter == null) {
                statement.setString(index, null);
            } else if (parameter instanceof String) {
                statement.setString(index, (String) parameter);
            } else if (parameter instanceof Integer) {
                statement.setInt(index, (Integer) parameter);
            } else if (parameter instanceof BigDecimal) {
                statement.setBigDecimal(index, (BigDecimal) parameter);
            } else if (parameter instanceof Boolean) {
                statement.setBoolean(index, (Boolean) parameter);
            } else if (parameter instanceof java.util.Date) {
                statement.setDate(index, toSqlDate((java.util.Date) parameter));
            } else {
                statement.setString(index, parameter.toString());
            }
            index += 1;
        }
    }

    private Date toSqlDate(java.util.Date sqlDate) {
        return new Date(sqlDate.getTime());
    }
}
