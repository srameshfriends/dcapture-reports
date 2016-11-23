package excel.accounting.db;

import org.h2.jdbcx.JdbcConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sql Result
 */
public class SqlResultSet implements Runnable {
    private final SqlQuery sqlQuery;
    private final JdbcConnectionPool pool;
    private final SqlReader notify;

    public SqlResultSet(JdbcConnectionPool pool) {
        this(pool, null, null);
    }

    SqlResultSet(JdbcConnectionPool pool, SqlQuery sqlQuery, SqlReader notify) {
        this.sqlQuery = sqlQuery;
        this.pool = pool;
        this.notify = notify;
    }

    public List<String> findStringList(SqlQuery sql) {
        List<Object> objectList = findObjectList(sql);
        return objectList.stream().map(object -> (String) object).collect(Collectors.toList());
    }

    public List<Integer> findIntegerList(SqlQuery sql) {
        List<Object> objectList = findObjectList(sql);
        return objectList.stream().map(object -> (Integer) object).collect(Collectors.toList());
    }

    public String[] findStrings(SqlQuery sql) {
        Object[] objects = findObjects(sql);
        if (objects != null) {
            String[] result = new String[objects.length];
            int index = 0;
            for (Object obj : objects) {
                result[index] = (String) obj;
                index += 1;
            }
            return result;
        }
        return null;
    }

    public String findString(SqlQuery sql) {
        return (String) findObject(sql);
    }

    public List<Object> loadObjectList(SqlQuery sql) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Object> resultList;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql.getQuery());
            addParameter(statement, sql);
            resultSet = statement.executeQuery();
            resultList = loadObjectList(resultSet);
            close(resultSet, statement, connection);
            return resultList;
        } catch (SQLException ex) {
            close(resultSet, statement, connection);
            throw ex;
        }
    }

    public List<Object[]> loadObjectsList(SqlQuery sql) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        List<Object[]> resultList;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql.getQuery());
            addParameter(statement, sql);
            result = statement.executeQuery();
            resultList = loadObjectsList(result.getMetaData().getColumnCount(), result);
            close(result, statement, connection);
            return resultList;
        } catch (SQLException ex) {
            close(result, statement, connection);
            throw ex;
        }
    }

    public Object findObject(SqlQuery sql) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Object object = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql.getQuery());
            addParameter(statement, sql);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                object = resultSet.getObject(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(resultSet, statement, connection);
        }
        return object;
    }

    public Object[] findObjects(SqlQuery sql) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Object[] objects = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql.getQuery());
            addParameter(statement, sql);
            result = statement.executeQuery();
            if (result.next()) {
                int columnCount = result.getMetaData().getColumnCount();
                objects = new Object[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    objects[col] = result.getObject(col + 1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(result, statement, connection);
        }
        return objects;
    }

    public List<Object> findObjectList(SqlQuery sql) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Object> resultList;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql.getQuery());
            addParameter(statement, sql);
            resultSet = statement.executeQuery();
            resultList = loadObjectList(resultSet);
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultList = new ArrayList<>();
        } finally {
            close(resultSet, statement, connection);
        }
        return resultList;
    }

    public List<Object[]> findObjectsList(SqlQuery sql) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        List<Object[]> resultList;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql.getQuery());
            addParameter(statement, sql);
            result = statement.executeQuery();
            resultList = loadObjectsList(result.getMetaData().getColumnCount(), result);
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultList = new ArrayList<>();
        } finally {
            close(result, statement, connection);
        }
        return resultList;
    }

    public SqlMetaDataResult findSqlMetaDataResult(SqlQuery sql) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        SqlMetaDataResult dataResult = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sql.getQuery());
            addParameter(statement, sql);
            result = statement.executeQuery();
            SqlMetaData[] metaData = getMetaData(result);
            List<Object[]> resultList = loadObjectsList(metaData.length, result);
            dataResult = new SqlMetaDataResult(metaData, resultList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(result, statement, connection);
        }
        return dataResult;
    }

    @Override
    public void run() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        List<Object[]> resultList;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement(sqlQuery.getQuery());
            addParameter(statement, sqlQuery);
            result = statement.executeQuery();
            SqlMetaData[] metaData = getMetaData(result);
            resultList = loadObjectsList(metaData.length, result);
            close(result, statement, connection);
            notify.onSqlResult(sqlQuery.getId(), metaData, resultList);
        } catch (SQLException ex) {
            close(result, statement, connection);
            notify.onSqlError(sqlQuery.getId(), ex);
        }
    }

    private List<Object> loadObjectList(ResultSet rs) throws SQLException {
        List<Object> dataList = new ArrayList<>();
        while (rs.next()) {
            Object data = rs.getObject(1);
            dataList.add(data);
        }
        return dataList;
    }

    private List<Object[]> loadObjectsList(int columnCount, ResultSet rs) throws SQLException {
        List<Object[]> dataList = new ArrayList<>();
        while (rs.next()) {
            Object[] data = new Object[columnCount];
            for (int col = 0; col < columnCount; col++) {
                data[col] = rs.getObject(col + 1);
            }
            dataList.add(data);
        }
        return dataList;
    }

    private SqlMetaData[] getMetaData(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        SqlMetaData[] metaDataArray = new SqlMetaData[rsMetaData.getColumnCount()];
        for (int index = 1; index <= metaDataArray.length; index++) {
            SqlMetaData metaData = new SqlMetaData();
            metaData.setColumnIndex(index);
            metaData.setTableName(rsMetaData.getTableName(index).toLowerCase());
            metaData.setColumnName(rsMetaData.getColumnName(index).toLowerCase());
            metaData.setColumnType(rsMetaData.getColumnType(index));
            metaDataArray[index - 1] = metaData;
        }
        return metaDataArray;
    }

    private void close(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception ex) {
                // ignore
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception ex) {
                // ignore
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex) {
                // ignore
            }
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

    private java.sql.Date toSqlDate(java.util.Date sqlDate) {
        return new java.sql.Date(sqlDate.getTime());
    }
}
