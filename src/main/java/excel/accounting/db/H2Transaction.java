package excel.accounting.db;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * H2 Transaction
 */
public class H2Transaction extends AbstractTransaction implements SqlTransaction {
    private final H2Processor processor;

    H2Transaction(H2Processor processor) {
        this.processor = processor;
    }

    private H2Processor getProcessor() {
        return processor;
    }

    @Override
    public void executeBatch(SqlQuery sqlQuery) throws SQLException {
        Connection connection = getProcessor().getConnectionPool().getConnection();
        connection.setAutoCommit(false);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlQuery.toString());
            addParameter(statement, sqlQuery.getParameterList());
            statement.addBatch();
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
    public void executeCommit(SqlQuery sqlQuery) throws SQLException {
        Connection connection = null;
        try {
            connection = getProcessor().getConnectionPool().getConnection();
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(sqlQuery.toString());
            addParameter(statement, sqlQuery.getParameterList());
            statement.execute();
            connection.commit();
            close(connection);
        } catch (SQLException ex) {
            rollback(connection);
            throw ex;
        }
    }

    @Override
    public SqlQuery insertQuery(Object object) {
        SqlTable table = getProcessor().getSqlTable(object.getClass());
        H2QueryBuilder builder = new H2QueryBuilder(getProcessor().getSchema());
        builder.insertInto(table.getName());
        for (SqlColumn sqlColumn : table) {
            Object fieldValue = getFieldObject(object, sqlColumn.getFieldName());
            builder.insertColumns(sqlColumn.getName(), fieldValue);
        }
        return builder.getSqlQuery();
    }

    @Override
    public SqlQuery updateQuery(Object object) {
        SqlTable table = getProcessor().getSqlTable(object.getClass());
        H2QueryBuilder builder = new H2QueryBuilder(getProcessor().getSchema());
        builder.update(table.getName());
        for (SqlColumn sqlColumn : table) {
            Object fieldValue = getFieldObject(object, sqlColumn.getFieldName());
            builder.updateColumns(sqlColumn.getName(), fieldValue);
        }
        return builder.getSqlQuery();
    }

    @Override
    public SqlQuery deleteQuery(Object object) {
        SqlTable table = getProcessor().getSqlTable(object.getClass());
        H2QueryBuilder builder = new H2QueryBuilder(getProcessor().getSchema());
        builder.deleteFrom(table.getName());
        SqlColumn sqlColumn = table.getPrimaryColumn();
        Object fieldValue = getFieldObject(object, sqlColumn.getFieldName());
        builder.where(sqlColumn.getName(), fieldValue);
        return builder.getSqlQuery();
    }

    @Override
    public void executeBatch(List<SqlQuery> queries) throws SQLException {
        Connection connection = getProcessor().getConnectionPool().getConnection();
        connection.setAutoCommit(false);
        try {
            SqlQuery sqlQuery = queries.get(0);
            PreparedStatement statement = connection.prepareStatement(sqlQuery.toString());
            for (SqlQuery sql : queries) {
                addParameter(statement, sql.getParameterList());
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
            connection = getProcessor().getConnectionPool().getConnection();
            connection.setAutoCommit(false);
            for (SqlQuery sql : queries) {
                PreparedStatement statement = connection.prepareStatement(sql.toString());
                addParameter(statement, sql.getParameterList());
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
