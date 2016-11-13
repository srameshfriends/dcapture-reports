package excel.accounting.db;

import org.apache.commons.beanutils.BeanUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orm Reader
 */
public class OrmReader {
    private OrmProcessor processor;

    public void setProcessor(OrmProcessor processor) {
        this.processor = processor;
    }

    public <T> List<T> findAll(OrmQuery ormQuery) {
        try {
            OrmTable ormTable = processor.getTable(ormQuery.getEntity());
            Map<String, String> columnMap = ormTable.getColumnFieldMap();
            Connection con = processor.getConnection();
            PreparedStatement stmt = con.prepareStatement(ormQuery.getQuery());
            if (!ormQuery.getParameterList().isEmpty()) {
                addParameter(stmt, ormQuery.getParameterList());
            }
            ResultSet result = stmt.executeQuery();
            List<T> dataList = new ArrayList<T>();
            while (result.next()) {
                T data = newInstance(ormQuery.getEntity());
                for (Map.Entry<String, String> entry : columnMap.entrySet()) {
                    BeanUtils.copyProperty(data, entry.getValue(), result.getObject(entry.getKey()));
                }
                dataList.add(data);
            }
            return dataList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<T>();
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

    private <T> T newInstance(Class<?> cls) {
        try {
            return (T) cls.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Date toSqlDate(java.util.Date sqlDate) {
        return new Date(sqlDate.getTime());
    }
}
