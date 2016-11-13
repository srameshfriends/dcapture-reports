package excel.accounting.db;

import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.sql.JDBCType;
import java.util.*;

/**
 * Orm Forward Tool
 */
class H2QueryTool implements QueryTool {
    private String schema;

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String createSchemaQuery() {
        return "create schema if not exists " + getSchema() + ";";
    }

    @Override
    public List<String> createTableQuery(Collection<OrmTable> tableList) {
        List<String> queryList = new ArrayList<>();
        for (OrmTable table : tableList) {
            List<OrmColumn> columnList = getSortedColumnList(table);
            table.setColumnList(columnList);
            String query = createTableQuery(getSchema(), table.getName(), columnList);
            queryList.add(query);
        }
        return queryList;
    }

    @Override
    public List<String> createReferenceQuery(Collection<OrmTable> tableList) {
        List<String> queryList = new ArrayList<>();
        for (OrmTable table : tableList) {
            List<String> queries = createReferenceQuery(getSchema(), table);
            queryList.addAll(queries);
        }
        return queryList;
    }

    @Override
    public List<OrmReference> createOrmReference(Collection<OrmTable> tableList) {
        List<OrmReference> referenceList = new ArrayList<>();
        for (OrmTable table : tableList) {
            List<OrmColumn> columnList = table.getColumnList();
            for (OrmColumn column : columnList) {
                if (column.getJoinTable() != null) {
                    OrmReference reference = new OrmReference();
                    reference.setOrmTable(column.getJoinTable());
                    reference.setOrmColumn(column.getJoinTable().getPrimaryColumn());
                    reference.setReferenceTable(table);
                    reference.setReferenceColumn(column);
                    referenceList.add(reference);
                }
            }
        }
        return referenceList;
    }

    private String getSchema() {
        return schema;
    }

    private int getMaxTextLength() {
        return 516;
    }

    private int getEnumLength() {
        return 16;
    }

    private List<String> createReferenceQuery(String schema, OrmTable ormTable) {
        List<String> referenceList = new ArrayList<>();
        for (OrmColumn column : ormTable.getColumnList()) {
            if (column.getJoinTable() != null) {
                StringBuilder builder = new StringBuilder("alter table ");
                builder.append(schema).append('.').append(ormTable.getName()).append(" add foreign key ");
                builder.append("(").append(column.getName()).append(") ");
                builder.append(" references ");
                OrmTable joinTable = column.getJoinTable();
                builder.append(schema).append(".").append(joinTable.getName()).append("(")
                        .append(joinTable.getPrimaryColumn().getName()).append(");");
                referenceList.add(builder.toString());
            }
        }
        return referenceList;
    }

    private List<OrmColumn> getSortedColumnList(OrmTable ormTable) {
        List<Class<?>> parentClassList = new ArrayList<>();
        parentClassList.add(ormTable.getType());
        findSuperClass(ormTable.getType(), parentClassList);
        Collections.reverse(parentClassList);
        List<String> columnList = new ArrayList<>();
        for (Class<?> cls : parentClassList) {
            ColumnIndex columnIndex = cls.getAnnotation(ColumnIndex.class);
            if (columnIndex != null && 0 < columnIndex.columns().length) {
                Collections.addAll(columnList, columnIndex.columns());
            }
        }
        List<OrmColumn> orderList = new ArrayList<>();
        List<OrmColumn> unOrderList = new ArrayList<>();
        for (String column : columnList) {
            for (OrmColumn ormColumn : ormTable.getColumnList()) {
                if (column.equals(ormColumn.getFieldName())) {
                    orderList.add(ormColumn);
                    break;
                }
            }
        }
        for (OrmColumn ormColumn : ormTable.getColumnList()) {
            if (!orderList.contains(ormColumn)) {
                unOrderList.add(ormColumn);
            }
        }
        orderList.addAll(unOrderList);
        return orderList;
    }

    private void findSuperClass(Class<?> cls, List<Class<?>> classList) {
        Class<?> parentClass = cls.getSuperclass();
        if (parentClass != null) {
            classList.add(parentClass);
            findSuperClass(parentClass, classList);
        }
    }

    private String createTableQuery(String schema, String table, List<OrmColumn> columnList) {
        StringBuilder builder = new StringBuilder("create table if not exists ");
        builder.append(schema).append('.').append(table).append("(");
        for (OrmColumn column : columnList) {
            setSQLType(column);
            builder.append(column.getName()).append(" ").append(getDataType(column));
            builder.append(", ");
        }
        builder.replace(builder.length() - 2, builder.length(), " ");
        for (OrmColumn column : columnList) {
            if (column.isPrimaryKey()) {
                builder.append(", primary key(").append(column.getName()).append(")");
                break;
            }
        }
        builder.append(");");
        return builder.toString();
    }

    private void setSQLType(final OrmColumn column) {
        final Class<?> type = column.getType();
        if (String.class.equals(type)) {
            column.setSqlType(JDBCType.VARCHAR);
        } else if (Date.class.equals(type)) {
            if (column.getTemporalType() != null && TemporalType.TIMESTAMP.equals(column.getTemporalType())) {
                column.setSqlType(JDBCType.TIMESTAMP);
            } else {
                column.setSqlType(JDBCType.DATE);
            }
        } else if (BigDecimal.class.equals(type)) {
            column.setSqlType(JDBCType.DECIMAL);
        } else if (Integer.class.equals(type) || int.class.equals(type)) {
            column.setSqlType(JDBCType.INTEGER);
        } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            column.setSqlType(JDBCType.BOOLEAN);
        } else if (Double.class.equals(type)) {
            column.setSqlType(JDBCType.DOUBLE);
        } else if (Enum.class.isAssignableFrom(type)) {
            column.setSqlType(JDBCType.VARCHAR);
        } else if (Long.class.equals(type)) {
            column.setSqlType(JDBCType.BIGINT);
        } else if (Short.class.equals(type)) {
            column.setSqlType(JDBCType.SMALLINT);
        } else if (Byte.class.equals(type)) {
            column.setSqlType(JDBCType.BINARY);
        } else {
            throw new IllegalArgumentException(column.getFieldName() + " sql data type not found " + column.getType());
        }
    }

    private String getDataType(final OrmColumn column) {
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
        } else if(Integer.class.equals(type)) {
            return "integer";
        } else if(Boolean.class.equals(type)) {
            return "boolean";
        } else if (Double.class.equals(type)) {
            return "double";
        } else if (Long.class.equals(type)) {
            return "bigint";
        }
        System.out.println(column.getType());
        throw new IllegalArgumentException("Unknown data type " + column.getFieldName());
    }

    @Override
    public String insertPreparedQuery(OrmTable ormTable) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(getSchema()).append(".").append(ormTable.getName()).append(" (");
        String param = "";
        for (OrmColumn ormColumn : ormTable.getColumnList()) {
            builder.append(ormColumn.getName()).append(",");
            param = param.concat("?,");
        }
        param = param.substring(0, param.length() - 1);
        builder.replace(builder.length() - 1, builder.length(), ") values(");
        builder.append(param).append(");");
        return builder.toString();
    }

    @Override
    public String selectQuery(OrmTable ormTable) {
        StringBuilder builder = new StringBuilder();
        builder.append("select ");
        for (OrmColumn ormColumn : ormTable.getColumnList()) {
            builder.append(ormColumn.getName()).append(",");
        }
        builder.replace(builder.length() - 1, builder.length(), " ");
        builder.append(" from ").append(getSchema()).append(".").append(ormTable.getName()).append(" ");
        return builder.toString();
    }
}
