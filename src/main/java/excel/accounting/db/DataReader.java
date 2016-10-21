package excel.accounting.db;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    public Object findObject(QueryBuilder query) {
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

    public List<Object[]> findObjects(QueryBuilder builder) {
        logger.info(builder.getQuery());
        try {
            List<Object[]> resultList = new ArrayList<>();
            Connection con = dataProcessor.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(builder.getQuery());
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] result = new Object[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    result[col] = rs.getObject(col + 1);
                }
                resultList.add(result);
            }
            close(rs, stmt, con);
            return resultList;
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return null;
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
