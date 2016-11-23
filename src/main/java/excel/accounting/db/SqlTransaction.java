package excel.accounting.db;

import org.h2.jdbcx.JdbcConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Sql Transaction
 */
public class SqlTransaction extends LinkedList<SqlQuery> implements Runnable {
    private boolean doBatchUpdate;
    private final JdbcConnectionPool pool;
    private SqlWriter response;
    private int processId;

    public SqlTransaction(JdbcConnectionPool pool) {
        this.pool = pool;
    }

    public void setDoBatchUpdate(boolean doBatchUpdate) {
        this.doBatchUpdate = doBatchUpdate;
    }

    public void setResponse(SqlWriter response) {
        this.response = response;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    @Override
    public void run() {
        try {
            Connection connection = pool.getConnection();
            connection.setAutoCommit(false);
            if (doBatchUpdate) {
                executeBatch(connection);
            } else {
                executeCommit(connection);
            }
            response.onSqlUpdated(processId);
        } catch (SQLException ex) {
            response.onSqlError(processId, ex);
        }
    }

    private void executeBatch(Connection connection) {
        try {
            SqlQuery sqlQuery = this.get(0);
            PreparedStatement statement = connection.prepareStatement(sqlQuery.getQuery());
            for (SqlQuery sql : this) {
                addParameter(statement, sql);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            close(statement);
            close(connection);
        } catch (SQLException ex) {
            rollback(connection);
            response.onSqlError(processId, ex);
        }
    }

    private void executeCommit(Connection connection) {
        try {
            for (SqlQuery sql : this) {
                PreparedStatement statement = connection.prepareStatement(sql.getQuery());
                addParameter(statement, sql);
                statement.execute();
            }
            connection.commit();
            close(connection);
        } catch (SQLException ex) {
            rollback(connection);
            response.onSqlError(processId, ex);
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
