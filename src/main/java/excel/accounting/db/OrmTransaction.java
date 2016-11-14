package excel.accounting.db;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Orm Transaction
 */
public class OrmTransaction {
    private static final Logger logger = Logger.getLogger(OrmTransaction.class);
    private final OrmProcessor processor;
    private List<OrmQuery> ormQueryList;

    public OrmTransaction(OrmProcessor processor) {
        this.processor = processor;
        ormQueryList = new ArrayList<>();
    }

    public void insert(Object object) throws Exception {
        OrmTable table = processor.getTable(object.getClass());
        if (table == null) {
            throw new RuntimeException(object.getClass() + " this is not a valid entity");
        }
        OrmQuery ormQuery = new OrmQuery();
        ormQuery.setQuery(processor.getQueryTool().insertPreparedQuery(table));
        List<OrmParameter> parameterList = new ArrayList<>();
        ormQuery.setParameterList(parameterList);
        int index = 1;
        for (OrmColumn ormColumn : table.getColumnList()) {
            Object fieldValue = getFieldObject(object, ormColumn.getFieldName());
            OrmParameter parameter = new OrmParameter(index, fieldValue, ormColumn.getSqlType());
            parameterList.add(parameter);
            index += 1;
        }
        ormQueryList.add(ormQuery);
    }

    public void update(Object object) throws Exception {
        OrmTable table = processor.getTable(object.getClass());
        if (table == null) {
            throw new RuntimeException(object.getClass() + " this is not a valid entity");
        }
        OrmQuery ormQuery = new OrmQuery();
        ormQuery.setQuery(processor.getQueryTool().updatePreparedQuery(table));
        List<OrmParameter> parameterList = new ArrayList<>();
        ormQuery.setParameterList(parameterList);
        Object fieldValue;
        OrmParameter parameter;
        int index = 1;
        for (OrmColumn ormColumn : table.getColumnList()) {
            fieldValue = getFieldObject(object, ormColumn.getFieldName());
            parameter = new OrmParameter(index, fieldValue, ormColumn.getSqlType());
            parameterList.add(parameter);
            index += 1;
        }
        OrmColumn ormColumn = table.getPrimaryColumn();
        fieldValue = getFieldObject(object, ormColumn.getFieldName());
        parameterList.add(new OrmParameter(index, fieldValue, ormColumn.getSqlType()));
        ormQueryList.add(ormQuery);
    }

    public void delete(Object object) throws Exception {
        OrmTable table = processor.getTable(object.getClass());
        if (table == null) {
            throw new RuntimeException(object.getClass() + " this is not a valid entity");
        }
        OrmColumn ormColumn = table.getPrimaryColumn();
        OrmQuery ormQuery = new OrmQuery();
        ormQuery.setQuery(processor.getQueryTool().deletePreparedQuery(table));
        List<OrmParameter> parameterList = new ArrayList<>();
        ormQuery.setParameterList(parameterList);
        Object fieldValue = getFieldObject(object, ormColumn.getFieldName());
        parameterList.add(new OrmParameter(1, fieldValue, ormColumn.getSqlType()));
        ormQueryList.add(ormQuery);
    }

    private Object getFieldObject(Object obj, String fieldName) throws Exception {
        try {
            return PropertyUtils.getProperty(obj, fieldName);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }
    }

    private void addParameter(PreparedStatement statement, List<OrmParameter> parameterList) throws Exception {
        for (OrmParameter param : parameterList) {
            addParameter(statement, param);
        }
    }

    private void addParameter(PreparedStatement statement, OrmParameter orm) throws Exception {
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
            if (dataType == null) {
                statement.setString(orm.getIndex(), param.toString());
            } else if (JDBCType.VARCHAR.equals(dataType)) {
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

    public void commitBatch() throws Exception {
        if (ormQueryList.isEmpty()) {
            return;
        }
        Connection connection = null;
        try {
            final String query = ormQueryList.get(0).getQuery();
            connection = processor.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(query);
            logger.info(query);
            for (OrmQuery ormQuery : ormQueryList) {
                addParameter(statement, ormQuery.getParameterList());
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            close(statement);
            close(connection);
            ormQueryList = new ArrayList<>();
        } catch (SQLException ex) {
            rollback(connection);
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    public void commit() throws Exception {
        if (ormQueryList.isEmpty()) {
            return;
        }
        Connection connection = null;
        try {
            List<PreparedStatement> statementList = new ArrayList<>();
            connection = processor.getConnection();
            for (OrmQuery ormQuery : ormQueryList) {
                PreparedStatement statement = connection.prepareStatement(ormQueryList.get(0).getQuery());
                addParameter(statement, ormQuery.getParameterList());
                statementList.add(statement);
                statement.execute();
            }
            connection.commit();
            close(statementList);
            close(connection);
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

    private void close(PreparedStatement statement) {
        try {
            statement.close();
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                if (logger.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                if (logger.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
