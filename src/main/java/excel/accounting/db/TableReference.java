package excel.accounting.db;

/**
 * Table Reference
 */
public class TableReference {
    private final String table, column, referenceTable, referenceColumn;

    public TableReference(String table, String column, String referenceTable, String referenceColumn) {
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
}
