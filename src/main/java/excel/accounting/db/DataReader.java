package excel.accounting.db;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data Reader
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class DataReader {
    private static final Logger logger = Logger.getLogger(Transaction.class);
    private final DataProcessor dataProcessor;

    public DataReader(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    private Connection getConnection() throws SQLException {
        return dataProcessor.getConnection();
    }

    public QueryBuilder getQueryBuilder(String fileName, String queryName) {
        return dataProcessor.getQueryBuilder(fileName, queryName);
    }

    public Object findSingleObject(QueryBuilder query) {
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

    public List<String> findString(QueryBuilder builder) {
        List<Object> objectList = findObject(builder);
        return objectList.stream().map(object -> (String) object).collect(Collectors.toList());
    }

    public List<Integer> findInteger(QueryBuilder builder) {
        List<Object> objectList = findObject(builder);
        return objectList.stream().map(object -> (Integer) object).collect(Collectors.toList());
    }

    public List<Object> findObject(QueryBuilder builder) {
        logger.debug(builder.getQuery());
        List<Object> resultList = new ArrayList<>();
        try {
            Connection con = dataProcessor.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(builder.getQuery());
            while (rs.next()) {
                resultList.add(rs.getObject(1));
            }
            close(rs, stmt, con);
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return resultList;
    }

    public List<Object[]> findObjects(QueryBuilder builder) {
        logger.info(builder.getQuery());
        List<Object[]> resultList = new ArrayList<>();
        try {
            Connection con = dataProcessor.getConnection();
            PreparedStatement statement = con.prepareStatement(builder.getQuery());
            addParameter(statement, builder.getParameters());
            ResultSet rs = statement.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] result = new Object[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    result[col] = rs.getObject(col + 1);
                }
                resultList.add(result);
            }
            close(rs, statement, con);
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return resultList;
    }

    public <T> List<T> findRowDataList(QueryBuilder builder, RowTypeConverter<T> converter) {
        List<T> dataList = new ArrayList<>();
        List<Object[]> objList = findObjects(builder);
        for (Object[] obj : objList) {
            T rowData = converter.getRowType(builder, obj);
            if (rowData != null) {
                dataList.add(rowData);
            }
        }
        return dataList;
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
            } else if (parameter instanceof DataType) {
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
                statement.setString(entry.getKey(), parameter.toString());
            }
        }
    }

    private java.sql.Date toSqlDate(java.util.Date sqlDate) {
        return new java.sql.Date(sqlDate.getTime());
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
}
