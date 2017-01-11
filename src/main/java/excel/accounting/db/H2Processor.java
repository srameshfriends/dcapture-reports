package excel.accounting.db;

import org.apache.commons.beanutils.PropertyUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

import javax.persistence.TemporalType;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * H2 Processor
 */
public class H2Processor implements SqlProcessor {
    private JdbcConnectionPool connectionPool;
    private SqlTableMap tableMap;
    private SqlEnumParser enumParser;
    private H2Reader reader;

    public static void main(String... args) throws Exception {
        Server.createTcpServer().start();
        Server.createWebServer().start();
    }

    void setConnectionPool(JdbcConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        reader = new H2Reader(connectionPool);
    }

    JdbcConnectionPool getConnectionPool() {
        return connectionPool;
    }

    void setTableMap(SqlTableMap tableMap) {
        this.tableMap = tableMap;
    }

    void setEnumParser(SqlEnumParser enumParser) {
        this.enumParser = enumParser;
    }

    @Override
    public String getSchema() {
        return tableMap.getSchema();
    }

    @Override
    public QueryBuilder createQueryBuilder() {
        return new H2QueryBuilder(getSchema());
    }

    @Override
    public SqlTableMap getSqlTableMap() {
        return tableMap;
    }

    @Override
    public SqlEnumParser enumParser() {
        return enumParser;
    }

    @Override
    public SqlReader getSqlReader() {
        return reader;
    }

    @Override
    public List<SqlReference> getSqlReference(Class<?> entityClass) {
        return null;
    }

    @Override
    public SqlQuery insertQuery(Object object) {
        SqlTable table = getSqlTableMap().get(object.getClass());
        if (table != null) {
            H2QueryBuilder builder = new H2QueryBuilder(getSchema());
            builder.insertInto(table.getName());
            for (SqlColumn sqlColumn : table) {
                Object fieldValue = getFieldObject(object, sqlColumn.getFieldName());
                builder.insertColumns(sqlColumn.getName(), fieldValue);
            }
            return builder.getSqlQuery();
        }
        return null;
    }

    @Override
    public SqlQuery updateQuery(Object object) {
        SqlTable table = getSqlTableMap().get(object.getClass());
        if (table != null) {
            H2QueryBuilder builder = new H2QueryBuilder(getSchema());
            builder.update(table.getName());
            for (SqlColumn sqlColumn : table) {
                Object fieldValue = getFieldObject(object, sqlColumn.getFieldName());
                builder.updateColumns(sqlColumn.getName(), fieldValue);
            }
            return builder.getSqlQuery();
        }
        return null;
    }

    @Override
    public SqlQuery deleteQuery(Object object) {
        SqlTable table = getSqlTableMap().get(object.getClass());
        if (table != null) {
            H2QueryBuilder builder = new H2QueryBuilder(getSchema());
            builder.deleteFrom(table.getName());
            SqlColumn sqlColumn = table.getPrimaryColumn();
            Object fieldValue = getFieldObject(object, sqlColumn.getFieldName());
            builder.where(sqlColumn.getName(), fieldValue);
            return builder.getSqlQuery();
        }
        return null;
    }

    private Object getFieldObject(Object obj, String fieldName) {
        try {
            return PropertyUtils.getProperty(obj, fieldName);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore Exception
        }
        return null;
    }

    @Override
    public H2QueryBuilder selectBuilder(Class<?> entityClass) {
        SqlTable sqlTable = getSqlTableMap().get(entityClass);
        if (sqlTable != null) {
            H2QueryBuilder builder = new H2QueryBuilder(getSchema());
            builder.selectFrom(sqlTable.getName());
            builder.selectColumns(sqlTable.getColumnFieldMap().keySet());
            return builder;
        }
        return null;
    }

    @Override
    public SqlQuery createSchemaQuery() {
        SqlQuery query = new SqlQuery();
        query.setQuery("create schema if not exists " + getSchema() + ";");
        return query;
    }

    @Override
    public List<SqlQuery> createTableQueries() {
        List<SqlQuery> queryList = new ArrayList<>();
        for (SqlTable table : getSqlTableMap().values()) {
            SqlQuery sqlQuery = new SqlQuery();
            sqlQuery.setQuery(createTableQuery(table.getName(), table));
            queryList.add(sqlQuery);
        }
        return queryList;
    }

    @Override
    public List<SqlQuery> alterTableQueries() {
        List<SqlQuery> queryList = new ArrayList<>();
        for (SqlTable table : getSqlTableMap().values()) {
            List<String> alterList = alterTableQuery(table);
            for (String alter : alterList) {
                SqlQuery query = new SqlQuery();
                query.setQuery(alter);
                queryList.add(query);
            }
        }
        return queryList;
    }

    @Override
    public SqlTransaction createSqlTransaction() {
        return new H2Transaction(this);
    }

    private String createTableQuery(String table, List<SqlColumn> columnList) {
        StringBuilder builder = new StringBuilder("create table if not exists ");
        builder.append(getSchema()).append('.').append(table).append("(");
        for (SqlColumn column : columnList) {
            builder.append(column.getName()).append(" ").append(getDataType(column));
            builder.append(", ");
        }
        builder.replace(builder.length() - 2, builder.length(), " ");
        for (SqlColumn column : columnList) {
            if (column.isPrimaryKey()) {
                builder.append(", primary key(").append(column.getName()).append(")");
                break;
            }
        }
        builder.append(");");
        return builder.toString();
    }

    private List<String> alterTableQuery(SqlTable sqlTable) {
        List<String> referenceList = new ArrayList<>();
        for (SqlColumn column : sqlTable) {
            if (column.getJoinTable() != null) {
                StringBuilder builder = new StringBuilder("alter table ");
                builder.append(getSchema()).append('.').append(sqlTable.getName()).append(" add foreign key ");
                builder.append("(").append(column.getName()).append(") ");
                builder.append(" references ");
                SqlTable joinTable = column.getJoinTable();
                builder.append(getSchema()).append(".").append(joinTable.getName()).append("(")
                        .append(joinTable.getPrimaryColumn().getName()).append(");");
                referenceList.add(builder.toString());
            }
        }
        return referenceList;
    }

    private int getMaxTextLength() {
        return 516;
    }

    private int getEnumLength() {
        return 16;
    }

    private String getDataType(final SqlColumn column) {
        final Class<?> type = column.getType();
        if (String.class.equals(type)) {
            String suffix = column.isNullable() ? "" : " not null";
            if (getMaxTextLength() < column.getLength()) {
                return "text".concat(suffix);
            }
            return "varchar(" + column.getLength() + ")" + suffix;
        } else if (Date.class.equals(type)) {
            if (column.getTemporalType() != null && TemporalType.TIMESTAMP.equals(column.getTemporalType())) {
                return "timestamp";
            }
            return "date";
        } else if (BigDecimal.class.equals(type)) {
            return "decimal";
        } else if (int.class.equals(type)) {
            return "integer";
        } else if (boolean.class.equals(type)) {
            return "boolean";
        } else if (double.class.equals(type)) {
            return "double";
        } else if (Enum.class.isAssignableFrom(type)) {
            return "varchar(" + getEnumLength() + ")";
        } else if (long.class.equals(type)) {
            return "bigint";
        } else if (Short.class.equals(type)) {
            return "smallint";
        } else if (Byte.class.equals(type)) {
            return "binary";
        } else if (Integer.class.equals(type)) {
            return "integer";
        } else if (Boolean.class.equals(type)) {
            return "boolean";
        } else if (Double.class.equals(type)) {
            return "double";
        } else if (Long.class.equals(type)) {
            return "bigint";
        }
        throw new IllegalArgumentException("Unknown data type " + column.getFieldName());
    }
}
