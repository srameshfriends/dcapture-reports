package excel.accounting.db;

import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.reflections.Reflections;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Orm Processor Class
 */
public class OrmProcessor implements Runnable {
    private Logger logger = Logger.getLogger(OrmProcessor.class);
    private Map<Class<?>, OrmTable> tableMap;
    private final JdbcConnectionPool connectionPool;
    private QueryTool queryTool;
    private Map<OrmTable, List<OrmReference>> ormReferenceMap;
    private String[] packageArray;
    private String schema;
    private OrmEnumParser ormEnumParser;

    public OrmProcessor(JdbcConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    JdbcConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public void setPackageArray(String... packageArray) {
        this.packageArray = packageArray;
    }

    String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public void run() {
        tableMap = createOrmTableMap(packageArray);
        addOrmTableColumns(tableMap);
        queryTool = new H2QueryTool();
        queryTool.setSchema(getSchema());
        String schemaQuery = queryTool.createSchemaQuery();
        List<String> tableQuery = queryTool.createTableQuery(tableMap.values());
        List<String> foreignQuery = queryTool.createReferenceQuery(tableMap.values());
        List<OrmReference> referenceList = queryTool.createOrmReference(tableMap.values());
        ormReferenceMap = new HashMap<>();
        for (OrmReference ref : referenceList) {
            if (!ormReferenceMap.containsKey(ref.getOrmTable())) {
                ormReferenceMap.put(ref.getOrmTable(), new ArrayList<>());
            }
            List<OrmReference> references = ormReferenceMap.get(ref.getOrmTable());
            references.add(ref);
        }
        Collections.unmodifiableMap(ormReferenceMap);
        executeUpdate(schemaQuery);
        tableQuery.forEach(this::executeUpdate);
        foreignQuery.forEach(this::executeUpdate);
    }

    Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    private void executeUpdate(String query) {
        Connection con = null;
        Statement stmt = null;
        try {
            logging(query);
            con = getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(stmt, con);
        }
    }

    private void close(Statement statement, Connection connection) {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Map<Class<?>, OrmTable> createOrmTableMap(String... packArray) {
        Map<Class<?>, OrmTable> tableMap = new HashMap<>();
        for (String pack : packArray) {
            Reflections reflections = new Reflections(pack.trim());
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Table.class);
            addOrmTableMap(tableMap, classes);
        }
        return tableMap;
    }

    private void addOrmTableMap(Map<Class<?>, OrmTable> tMap, Set<Class<?>> entityList) {
        if (entityList != null && !entityList.isEmpty()) {
            for (Class<?> clazz : entityList) {
                tMap.put(clazz, null);
            }
        }
    }

    private void addOrmTableColumns(Map<Class<?>, OrmTable> classTableMap) {
        logging("Generate object relational mapping");
        for (Class<?> entity : classTableMap.keySet()) {
            String tableName = findTableName(entity);
            if (tableName == null) {
                continue;
            }
            List<Field> columnList = new ArrayList<>();
            OrmTable sqlTable = new OrmTable(tableName, entity);
            sqlTable.setFieldList(columnList);
            addFields(columnList, entity);
            classTableMap.put(sqlTable.getType(), sqlTable);
        }
        logging("Read orm primary columns and another columns");
        Collection<OrmTable> tableList = classTableMap.values();
        tableList.forEach(this::createColumn);
        logging("Read table join columns");
        for (OrmTable sqlTable : tableList) {
            createJoinColumn(sqlTable, tableList);
        }
    }

    OrmTable getTable(Class<?> type) {
        return tableMap.get(type);
    }

    QueryTool getQueryTool() {
        return queryTool;
    }

    public void setOrmEnumParser(OrmEnumParser ormEnumParser) {
        this.ormEnumParser = ormEnumParser;
    }

    Object getEnum(Class<?> enumClass, String name) {
        return ormEnumParser.getEnum(enumClass, name);
    }

    private String findTableName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        Table table = clazz.getAnnotation(Table.class);
        return table == null ? null : table.name();
    }

    private OrmTable findJoinTable(String tableName, Collection<OrmTable> tableList) {
        if (tableName == null) {
            return null;
        }
        for (OrmTable table : tableList) {
            if (tableName.equals(table.getName())) {
                return table;
            }
        }
        return null;
    }

    private void addFields(List<Field> fieldList, Class<?> classType) {
        Field[] fieldArray = classType.getDeclaredFields();
        if (fieldArray.length > 0) {
            Collections.addAll(fieldList, fieldArray);
        }
        if (classType.getSuperclass() != null) {
            if (!Object.class.equals(classType.getSuperclass())) {
                addFields(fieldList, classType.getSuperclass());
            }
        }
    }

    private void createColumn(OrmTable sqlTable) {
        List<OrmColumn> columnList = new ArrayList<>();
        sqlTable.setColumnList(columnList);
        for (Field field : sqlTable.getFieldList()) {
            Column column = field.getAnnotation(Column.class);
            OrmColumn ormColumn = null;
            if (column != null) {
                ormColumn = createOrmColumn(field, column);
            }
            Id primaryId = field.getAnnotation(Id.class);
            if (primaryId != null) {
                if (ormColumn == null) {
                    ormColumn = new OrmColumn(field.getName(), field.getType());
                }
                ormColumn.setPrimaryKey(true);
                GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
                if (generatedValue != null) {
                    ormColumn.setAutoIncrement(true);
                }
            }
            if (ormColumn != null) {
                columnList.add(ormColumn);
            }
        }
    }

    private OrmColumn createOrmColumn(Field field, Column column) {
        final String name = column.name().length() != 0 ? column.name() : field.getName();
        OrmColumn sqlColumn = new OrmColumn(name, field.getType());
        sqlColumn.setFieldName(field.getName());
        sqlColumn.setNullable(column.nullable());
        sqlColumn.setLength(column.length());
        if (Date.class.equals(sqlColumn.getType())) {
            Temporal temporal = field.getAnnotation(Temporal.class);
            sqlColumn.setTemporalType(temporal == null ? null : temporal.value());
        }
        return sqlColumn;
    }

    private void createJoinColumn(final OrmTable sqlTable, final Collection<OrmTable> tableList) {
        for (Field field : sqlTable.getFieldList()) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                OrmTable joinTable = findJoinTable(joinColumn.table(), tableList);
                if (joinTable == null) {
                    String err = sqlTable.getType() + " join column table name missing " + field.getName();
                    throw new NullPointerException(err);
                }
                OrmColumn sqlColumn = createJoinColumn(field, joinColumn);
                sqlColumn.setJoinTable(joinTable);
                sqlColumn.setLength(joinTable.getPrimaryColumn().getLength());
                sqlTable.getColumnList().add(sqlColumn);
            }
        }
    }

    private OrmColumn createJoinColumn(Field field, JoinColumn joinColumn) {
        final String name = joinColumn.name().length() != 0 ? joinColumn.name() : field.getName();
        OrmColumn sqlColumn = new OrmColumn(name, field.getType());
        sqlColumn.setFieldName(field.getName());
        sqlColumn.setNullable(joinColumn.nullable());
        return sqlColumn;
    }

    private void logging(String value) {
        logger.info(value);
    }
}
