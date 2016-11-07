package excel.accounting.db;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

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
    private PreparedStatement batchStatement;

    public Transaction(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = dataProcessor.getConnection();
            connection.setAutoCommit(false);
        }
        return connection;
    }

    public void execute(QueryBuilder builder) throws SQLException {
        if (batchStatement != null) {
            throw new RuntimeException("Batch transaction is enabled, should not be use execute");
        }
        if (statementMap == null) {
            statementMap = new HashMap<>();
        }
        PreparedStatement statement = getConnection().prepareStatement(builder.getQuery());
        addParameter(statement, builder.getParameters());
        statementMap.put(builder, statement);
    }

    public void setBatchQuery(QueryBuilder builder) {
        if (statementMap != null) {
            throw new RuntimeException("Execute query added, Batch transaction not able to use");
        }
        if (batchStatement != null) {
            throw new RuntimeException("Already batch query updated, can not be override");
        }
        try {
            batchStatement = getConnection().prepareStatement(builder.getQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBatch(Map<Integer, Object> parameterMap) {
        if (statementMap != null) {
            throw new RuntimeException("Execute query added, Batch transaction not able to use");
        }
        if (batchStatement == null) {
            throw new NullPointerException("Batch query should not be null");
        }
        try {
            addParameter(batchStatement, parameterMap);
            batchStatement.addBatch();
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void addBatch(Object... parameterArray) {
        Map<Integer, Object> parameterMap = new HashMap<>();
        int index = 1;
        for (Object obj : parameterArray) {
            parameterMap.put(index, obj);
            index += 1;
        }
        addBatch(parameterMap);
    }

    public void addBatch(List<Object> parameterArray) {
        Map<Integer, Object> parameterMap = new HashMap<>();
        int index = 1;
        for (Object obj : parameterArray) {
            parameterMap.put(index, obj);
            index += 1;
        }
        addBatch(parameterMap);
    }

    public void executeBatch() throws SQLException {
        if (batchStatement == null) {
            throw new RuntimeException("Batch transaction is disabled");
        }
        try {
            batchStatement.executeBatch();
            connection.commit();
            close(connection);
            connection = null;
            batchStatement = null;
        } catch (SQLException e) {
            rollback(connection);
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw e;
        }
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
            } else if (parameter instanceof java.util.Date) {
                statement.setDate(entry.getKey(), toSqlDate((java.util.Date) parameter));
            } else if(parameter instanceof  DataType) {
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
            } else {
                System.out.println(parameter.getClass());
                statement.setString(entry.getKey(), parameter.toString());
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

    private java.sql.Date toSqlDate(java.util.Date sqlDate) {
        return new java.sql.Date(sqlDate.getTime());
    }
}

