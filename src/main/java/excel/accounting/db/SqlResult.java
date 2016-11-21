package excel.accounting.db;

import org.h2.jdbcx.JdbcConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Sql Result
 */
class SqlResult implements Runnable {
    private final SqlQuery sqlQuery;
    private final JdbcConnectionPool pool;
    private final SqlReadResponse response;

    SqlResult(JdbcConnectionPool pool, SqlQuery sqlQuery, SqlReadResponse response) {
        this.sqlQuery = sqlQuery;
        this.pool = pool;
        this.response = response;
    }

    @Override
    public void run() {
        try {
            String query = sqlQuery.getQuery();
            Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            addParameter(statement, sqlQuery);
            ResultSet rs = statement.executeQuery();
            SqlMetaData[] metaDataArray = getMetaData(rs);
            List<Object[]> dataArrayList = findObjectList(metaDataArray.length, rs);
            close(rs, statement, connection);
            response.onSqlResponse(sqlQuery, metaDataArray, dataArrayList);
        } catch (SQLException ex) {
            response.onSqlError(sqlQuery, ex);
        }
    }

    private List<Object[]> findObjectList(int columnCount, ResultSet rs) throws SQLException {
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

    private void close(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            resultSet.close();
        } catch (Exception ex) {
            // ignore
        }
        try {
            statement.close();
        } catch (Exception ex) {
            // ignore
        }
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

    private java.sql.Date toSqlDate(java.util.Date sqlDate) {
        return new java.sql.Date(sqlDate.getTime());
    }
}
