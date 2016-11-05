package excel.accounting.db;

/**
 * Table Reference
 */
public class EntityReference {
    private final String table, column, referenceTable, referenceColumn;

    public EntityReference(String table, String column, String referenceTable, String referenceColumn) {
        this.table = table;
        this.column = column;
        this.referenceTable = referenceTable;
        this.referenceColumn = referenceColumn;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    public String getReferenceTable() {
        return referenceTable;
    }

    public String getReferenceColumn() {
        return referenceColumn;
    }

    String getQuery() {
        String query = "SELECT ".concat(column).concat(" FROM ").concat(table).concat(" WHERE ").concat(column);
        return query.concat(" = ?");
    }

    EntityReference newInstance() {
        return new EntityReference(table, column, referenceTable, referenceColumn);
    }
}
