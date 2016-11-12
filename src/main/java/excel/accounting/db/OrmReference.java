package excel.accounting.db;

/**
 * Orm Reference
 */
public class OrmReference {
    private OrmTable ormTable, referenceTable;
    private OrmColumn ormColumn, referenceColumn;

    public OrmTable getOrmTable() {
        return ormTable;
    }

    public void setOrmTable(OrmTable ormTable) {
        this.ormTable = ormTable;
    }

    public OrmTable getReferenceTable() {
        return referenceTable;
    }

    public void setReferenceTable(OrmTable referenceTable) {
        this.referenceTable = referenceTable;
    }

    public OrmColumn getOrmColumn() {
        return ormColumn;
    }

    public void setOrmColumn(OrmColumn ormColumn) {
        this.ormColumn = ormColumn;
    }

    public OrmColumn getReferenceColumn() {
        return referenceColumn;
    }

    public void setReferenceColumn(OrmColumn referenceColumn) {
        this.referenceColumn = referenceColumn;
    }
}
