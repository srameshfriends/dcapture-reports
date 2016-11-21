package excel.accounting.db;

/**
 * Sql Reference
 */
public class SqlReference {
    private SqlTable sqlTable, referenceTable;
    private SqlColumn sqlColumn, referenceColumn;

    public SqlTable getSqlTable() {
        return sqlTable;
    }

    public void setSqlTable(SqlTable sqlTable) {
        this.sqlTable = sqlTable;
    }

    public SqlTable getReferenceTable() {
        return referenceTable;
    }

    public void setReferenceTable(SqlTable referenceTable) {
        this.referenceTable = referenceTable;
    }

    public SqlColumn getSqlColumn() {
        return sqlColumn;
    }

    public void setSqlColumn(SqlColumn sqlColumn) {
        this.sqlColumn = sqlColumn;
    }

    public SqlColumn getReferenceColumn() {
        return referenceColumn;
    }

    public void setReferenceColumn(SqlColumn referenceColumn) {
        this.referenceColumn = referenceColumn;
    }
}
