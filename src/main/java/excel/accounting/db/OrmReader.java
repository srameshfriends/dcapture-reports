package excel.accounting.db;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.ltgfmt.FileDesc;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orm Reader
 */
public class OrmReader {
    private static final Logger logger = Logger.getLogger(OrmReader.class);
    private OrmProcessor processor;

    public void setProcessor(OrmProcessor processor) {
        this.processor = processor;
    }

    public <T> List<T> findAll(OrmQuery ormQuery) {
        try {
            OrmTable ormTable = processor.getTable(ormQuery.getEntity());
            if (ormTable == null) {
                throw new NullPointerException("Orm table should not be empty");
            }
            Map<String, OrmColumn> columnMap = ormTable.getColumnFieldMap();
            Connection con = processor.getConnection();
            PreparedStatement stmt = con.prepareStatement(ormQuery.getQuery());
            if (!ormQuery.getParameterList().isEmpty()) {
                addParameter(stmt, ormQuery.getParameterList());
            }
            ResultSet result = stmt.executeQuery();
            List<T> dataList = new ArrayList<T>();
            while (result.next()) {
                T data = newInstance(ormQuery.getEntity());
                for (Map.Entry<String, OrmColumn> entry : columnMap.entrySet()) {
                    Object columnObj = result.getObject(entry.getKey());
                    if (entry.getValue().isEnumClass()) {
                        columnObj = getEnum(entry.getValue().getType(), columnObj);
                    }
                    BeanUtils.copyProperty(data, entry.getValue().getFieldName(), columnObj);
                }
                dataList.add(data);
            }
            return dataList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<T>();
    }

    public List<Object> findObjectList(OrmQuery ormQuery) {
        logger.debug(ormQuery.getQuery());
        List<Object> resultList = new ArrayList<>();
        try {
            Connection con = processor.getConnection();
            PreparedStatement stmt = con.prepareStatement(ormQuery.getQuery());
            addParameter(stmt, ormQuery.getParameterList());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultList.add(rs.getObject(1));
            }
            close(rs, stmt, con);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return resultList;
    }

    public List<String> findStringList(OrmQuery ormQuery) {
        List<Object> objectList = findObjectList(ormQuery);
        return objectList.stream().map(object -> (String) object).collect(Collectors.toList());
    }

    public List<Integer> findIntegerList(OrmQuery ormQuery) {
        List<Object> objectList = findObjectList(ormQuery);
        return objectList.stream().map(object -> (Integer) object).collect(Collectors.toList());
    }

    private void close(ResultSet resultSet, PreparedStatement statement, Connection connection) {
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

    private Object getEnum(Class<?> enumClass, Object name) {
        return name == null ? null : processor.getEnum(enumClass, name.toString());
    }

    private Date toSqlDate(java.util.Date sqlDate) {
        return new Date(sqlDate.getTime());
    }
}
