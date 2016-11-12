package excel.accounting.db;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Orm Table
 */
@Entity
public class OrmTable {
    private final Class<?> type;
    private final String name;
    private List<Field> fieldList;
    private List<OrmColumn> columnList;
    private OrmColumn primaryColumn;
    private Map<OrmTable, OrmColumn> referenceMap;

    public OrmTable(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setFieldList(List<Field> fieldList) {
        this.fieldList = fieldList;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public List<OrmColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<OrmColumn> columnList) {
        this.columnList = columnList;
    }

    public OrmColumn getPrimaryColumn() {
        if(primaryColumn == null) {
            for(OrmColumn column : columnList) {
                if(column.isPrimaryKey()) {
                    primaryColumn = column;
                    break;
                }
            }
        }
        return primaryColumn;
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
        return obj != null && obj instanceof OrmTable && ((OrmTable)obj).getName().equals(getName());
    }
}
