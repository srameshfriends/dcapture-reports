package excel.accounting.db;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sql Factory
 */
public abstract class SqlFactory {
    private static Logger logger;

    public static void setLogger(Logger logger) {
        SqlFactory.logger = logger;
    }

    private static void logging(String value) {
        logger.info(value);
    }

    public static SqlTableMap createSqlTableMap(String schema, String... packArray) {
        SqlTableMap tableMap = new SqlTableMap(schema);
        for (String pack : packArray) {
            Reflections reflections = new Reflections(pack.trim());
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Table.class);
            addTableClassMap(tableMap, classes);
        }
        addTableColumns(tableMap);
        return tableMap;
    }

    private static void addTableClassMap(Map<Class<?>, SqlTable> tMap, Set<Class<?>> entityList) {
        if (entityList != null && !entityList.isEmpty()) {
            for (Class<?> clazz : entityList) {
                tMap.put(clazz, null);
            }
        }
    }

    private static void addTableColumns(SqlTableMap tableMap) {
        for (Class<?> entity : tableMap.keySet()) {
            String tableName = findTableName(entity);
            if (tableName == null) {
                continue;
            }
            List<Field> columnList = new ArrayList<>();
            SqlTable sqlTable = new SqlTable(tableName, entity);
            sqlTable.setFieldList(columnList);
            addFields(columnList, entity);
            tableMap.put(sqlTable.getType(), sqlTable);
        }
        Collection<SqlTable> tableList = tableMap.values();
        for (SqlTable table : tableList) {
            table.addAll(createColumns(table));
        }
        for (SqlTable table : tableList) {
            createJoinColumn(table, tableList);
        }
        for (SqlTable table : tableList) {
            List<SqlColumn> sortedList = getSortedColumnList(table);
            table.clear();
            table.addAll(sortedList);
        }
        List<SqlReference> refList = new ArrayList<>();
        for (SqlTable table : tableList) {
            table.stream().filter(column -> column.getJoinTable() != null).forEach(column -> {
                SqlReference reference = new SqlReference();
                reference.setSqlTable(column.getJoinTable());
                reference.setSqlColumn(column.getJoinTable().getPrimaryColumn());
                reference.setReferenceTable(table);
                reference.setReferenceColumn(column);
                refList.add(reference);
            });
        }
        for (SqlTable table : tableList) {
            List<SqlReference> list = refList.stream().filter(reference ->
                    table.equals(reference.getSqlTable())).collect(Collectors.toList());
            if(!list.isEmpty()) {
                table.setReferenceList(list);
            }
        }
    }

    private static String findTableName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        Table table = clazz.getAnnotation(Table.class);
        return table == null ? null : table.name();
    }

    private static SqlTable findJoinTable(String tableName, Collection<SqlTable> tableList) {
        if (tableName == null) {
            return null;
        }
        for (SqlTable table : tableList) {
            if (tableName.equals(table.getName())) {
                return table;
            }
        }
        return null;
    }

    private static void addFields(List<Field> fieldList, Class<?> classType) {
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

    private static List<SqlColumn> createColumns(SqlTable sqlTable) {
        List<SqlColumn> columnList = new ArrayList<>();
        sqlTable.addAll(columnList);
        for (Field field : sqlTable.getFieldList()) {
            Column column = field.getAnnotation(Column.class);
            SqlColumn sqlColumn = null;
            if (column != null) {
                sqlColumn = createColumn(field, column);
            }
            Id primaryId = field.getAnnotation(Id.class);
            if (primaryId != null) {
                if (sqlColumn == null) {
                    sqlColumn = new SqlColumn(field.getName(), field.getType());
                }
                sqlColumn.setPrimaryKey(true);
                GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
                if (generatedValue != null) {
                    sqlColumn.setAutoIncrement(true);
                }
            }
            if (sqlColumn != null) {
                columnList.add(sqlColumn);
            }
        }
        return columnList;
    }

    private static SqlColumn createColumn(Field field, Column column) {
        final String name = column.name().length() != 0 ? column.name() : field.getName();
        SqlColumn sqlColumn = new SqlColumn(name, field.getType());
        sqlColumn.setFieldName(field.getName());
        sqlColumn.setNullable(column.nullable());
        sqlColumn.setLength(column.length());
        if (Date.class.equals(sqlColumn.getType())) {
            Temporal temporal = field.getAnnotation(Temporal.class);
            sqlColumn.setTemporalType(temporal == null ? null : temporal.value());
        }
        return sqlColumn;
    }

    private static void createJoinColumn(final SqlTable sqlTable, final Collection<SqlTable> tableList) {
        for (Field field : sqlTable.getFieldList()) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                SqlTable joinTable = findJoinTable(joinColumn.table(), tableList);
                if (joinTable == null) {
                    String err = sqlTable.getType() + " join column table name missing " + field.getName();
                    throw new NullPointerException(err);
                }
                SqlColumn sqlColumn = createJoinColumn(field, joinColumn);
                sqlColumn.setJoinTable(joinTable);
                sqlColumn.setLength(joinTable.getPrimaryColumn().getLength());
                sqlTable.add(sqlColumn);
            }
        }
    }

    private static SqlColumn createJoinColumn(Field field, JoinColumn joinColumn) {
        final String name = joinColumn.name().length() != 0 ? joinColumn.name() : field.getName();
        SqlColumn sqlColumn = new SqlColumn(name, field.getType());
        sqlColumn.setFieldName(field.getName());
        sqlColumn.setNullable(joinColumn.nullable());
        return sqlColumn;
    }

    private static List<SqlColumn> getSortedColumnList(SqlTable sqlTable) {
        List<Class<?>> parentClassList = new ArrayList<>();
        parentClassList.add(sqlTable.getType());
        findSuperClass(sqlTable.getType(), parentClassList);
        Collections.reverse(parentClassList);
        List<String> columnList = new ArrayList<>();
        for (Class<?> cls : parentClassList) {
            ColumnIndex columnIndex = cls.getAnnotation(ColumnIndex.class);
            if (columnIndex != null && 0 < columnIndex.columns().length) {
                Collections.addAll(columnList, columnIndex.columns());
            }
        }
        List<SqlColumn> orderList = new ArrayList<>();
        List<SqlColumn> unOrderList = new ArrayList<>();
        for (String column : columnList) {
            for (SqlColumn sqlColumn : sqlTable) {
                if (column.equals(sqlColumn.getFieldName())) {
                    orderList.add(sqlColumn);
                    break;
                }
            }
        }
        for (SqlColumn sqlColumn : sqlTable) {
            if (!orderList.contains(sqlColumn)) {
                unOrderList.add(sqlColumn);
            }
        }
        orderList.addAll(unOrderList);
        return orderList;
    }

    private static void findSuperClass(Class<?> cls, List<Class<?>> classList) {
        Class<?> parentClass = cls.getSuperclass();
        if (parentClass != null) {
            classList.add(parentClass);
            findSuperClass(parentClass, classList);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> List<T> toEntityList(SqlTableMap tableMap, SqlEnumParser enumParsers, SqlMetaData[] metaData,
                                    List<Object[]> dataList) {
        SqlTable table = dataList.isEmpty() ? null : getValidOrmTable(tableMap, metaData);
        if (table == null) {
            return new ArrayList<T>();
        }
        List<T> resultList = new ArrayList<>();
        for (Object[] data : dataList) {
            T result = (T) instance(table.getType());
            int index = 0;
            for (Object value : data) {
                String columnName = metaData[index].getColumnName();
                String fieldName = table.getColumnFieldMap().get(columnName);
                Class<?> enumClass = table.getEnumClass(fieldName);
                if (enumClass != null) {
                    value = enumParsers.getEnum(enumClass, (String) value);
                }
                copyProperty(result, fieldName, value);
                index += 1;
            }
            resultList.add(result);
        }
        return resultList;
    }

    private static void copyProperty(Object bean, String name, Object value) {
        try {
            BeanUtils.copyProperty(bean, name, value);
        } catch (Exception ex) {
            // ignore
        }
    }

    private static SqlTable getValidOrmTable(SqlTableMap tableMap, SqlMetaData[] metaDataArray) {
        SqlTable table = tableMap.getSqlTable(metaDataArray[0].getTableName());
        if (table != null) {
            for (SqlMetaData metaData : metaDataArray) {
                if (!metaData.getTableName().equals(table.getName())) {
                    return null;
                }
            }
        }
        return table;
    }

    private static Object instance(Class<?> ins) {
        try {
            return ins.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
