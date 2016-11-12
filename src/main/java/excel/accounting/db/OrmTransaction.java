package excel.accounting.db;

import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orm Transaction
 */
public class OrmTransaction {
    private static final Logger logger = Logger.getLogger(OrmTransaction.class);
    private final JdbcConnectionPool connectionPool;
    private Map<OrmQuery, PreparedStatement> statementMap;
    private Connection connection;
    private PreparedStatement batchStatement;

    OrmTransaction(JdbcConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    void execute(OrmQuery ormQuery) throws SQLException {
        if (batchStatement != null) {
            throw new RuntimeException("Batch transaction is enabled, should not be use execute");
        }
        if (statementMap == null) {
            statementMap = new HashMap<>();
        }
        PreparedStatement statement = getConnection().prepareStatement(ormQuery.getQuery());
        addParameter(statement, ormQuery.getParameterList());
        statementMap.put(ormQuery, statement);
    }

    private void addParameter(PreparedStatement statement, List<OrmParameter> parameterList) throws SQLException {
        for (OrmParameter param : parameterList) {
            addParameter(statement, param);
        }
    }

    private void addParameter(PreparedStatement statement, OrmParameter orm) throws SQLException {
        Object param = orm.getParameter();
        if (param instanceof String) {
            statement.setString(orm.getIndex(), (String) param);
        } else if (param instanceof Integer) {
            statement.setInt(orm.getIndex(), (Integer) param);
        } else if (param instanceof BigDecimal) {
            statement.setBigDecimal(orm.getIndex(), (BigDecimal) param);
        } else if (param instanceof Boolean) {
            statement.setBoolean(orm.getIndex(), (Boolean) param);
        } else if (param instanceof java.util.Date) {
            statement.setDate(orm.getIndex(), toSqlDate((java.util.Date) param));
        } else if (param instanceof Enum) {
            statement.setString(orm.getIndex(), param.toString());
        } else {
            SQLType dataType = orm.getSqlType();
            if (JDBCType.VARCHAR.equals(dataType)) {
                statement.setString(orm.getIndex(), null);
            } else if (JDBCType.DATE.equals(dataType)) {
                statement.setDate(orm.getIndex(), null);
            } else if (JDBCType.TIMESTAMP.equals(dataType)) {
                statement.setDate(orm.getIndex(), null);
            } else if (JDBCType.INTEGER.equals(dataType)) {
                statement.setInt(orm.getIndex(), 0);
            } else if (JDBCType.DOUBLE.equals(dataType)) {
                statement.setDouble(orm.getIndex(), 0);
            } else if (JDBCType.DECIMAL.equals(dataType)) {
                statement.setBigDecimal(orm.getIndex(), BigDecimal.ZERO);
            } else if (JDBCType.BOOLEAN.equals(dataType)) {
                statement.setBoolean(orm.getIndex(), false);
            }
        }
    }

    private Date toSqlDate(java.util.Date sqlDate) {
        return new Date(sqlDate.getTime());
    }

    public void commit() {
        if (batchStatement != null) {
            throw new RuntimeException("Batch transaction is disabled!");
        }
        try {
            for (PreparedStatement statement : statementMap.values()) {
                statement.execute();
            }
            connection.commit();
            close(statementMap.values());
            close(connection);
            statementMap = null;
            connection = null;
        } catch (SQLException ex) {
            rollback(connection);
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void close(Collection<PreparedStatement> statementList) {
        try {
            for (PreparedStatement statement : statementList) {
                statement.close();
            }
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = connectionPool.getConnection();
            connection.setAutoCommit(false);
        }
        return connection;
    }
}
