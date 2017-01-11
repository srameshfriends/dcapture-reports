package excel.accounting.db;

/**
 * Sql Reference
 */
public class SqlReference {
    private SqlTable sqlTable, referenceTable;
    private SqlColumn sqlColumn, referenceColumn;

    SqlTable getSqlTable() {
        return sqlTable;
    }

    void setSqlTable(SqlTable sqlTable) {
        this.sqlTable = sqlTable;
    }

    SqlTable getReferenceTable() {
        return referenceTable;
    }

    void setReferenceTable(SqlTable referenceTable) {
        this.referenceTable = referenceTable;
    }

    SqlColumn getSqlColumn() {
        return sqlColumn;
    }

    void setSqlColumn(SqlColumn sqlColumn) {
        this.sqlColumn = sqlColumn;
    }

    SqlColumn getReferenceColumn() {
        return referenceColumn;
    }

    void setReferenceColumn(SqlColumn referenceColumn) {
        this.referenceColumn = referenceColumn;
    }
}
