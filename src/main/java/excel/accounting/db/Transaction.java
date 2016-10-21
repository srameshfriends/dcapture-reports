package excel.accounting.db;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Transaction
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class Transaction {
    private static final Logger logger = Logger.getLogger(Transaction.class);
    private final DataProcessor dataProcessor;
    private Connection connection;
    private Map<QueryBuilder, PreparedStatement> statementMap;

    public Transaction(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = dataProcessor.getConnection();
            connection.setAutoCommit(false);
            statementMap = new HashMap<>();
        }
        return connection;
    }

    public void execute(QueryBuilder builder) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(builder.getQuery());
            addParameter(statement, builder.getParameters());
            statementMap.put(builder, statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void commit() {
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
            ex.printStackTrace();
        }
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

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
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
}

