package excel.accounting.db;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Sql Table
 */
class SqlTable extends ArrayList<SqlColumn> {
    private final Class<?> type;
    private final String name;
    private List<Field> fieldList;
    private SqlColumn primaryColumn;
    private Map<String, String> columnFieldMap;
    private Map<String, Class<?>> enumFieldMap;
    private List<SqlReference> referenceList;

    SqlTable(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    void setFieldList(List<Field> fieldList) {
        this.fieldList = fieldList;
    }

    List<Field> getFieldList() {
        return fieldList;
    }

    List<SqlReference> getReferenceList() {
        return referenceList;
    }

    void setReferenceList(List<SqlReference> referenceList) {
        this.referenceList = referenceList;
    }

    SqlColumn getPrimaryColumn() {
        if (primaryColumn == null) {
            for (SqlColumn column : this) {
                if (column.isPrimaryKey()) {
                    primaryColumn = column;
                    break;
                }
            }
        }
        return primaryColumn;
    }

    Map<String, String> getColumnFieldMap() {
        if (columnFieldMap == null) {
            columnFieldMap = new HashMap<>();
            for (SqlColumn column : this) {
                columnFieldMap.put(column.getName(), column.getFieldName());
            }
        }
        return columnFieldMap;
    }

    Class<?> getEnumClass(String fieldName) {
        if (enumFieldMap == null) {
            enumFieldMap = new HashMap<>();
            for (Field field : fieldList) {
                if (field.getType().isEnum()) {
                    enumFieldMap.put(field.getName(), field.getType());
                }
            }
        }
        return enumFieldMap.get(fieldName);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof SqlTable && ((SqlTable) obj).getName().equals(getName());
    }
}
