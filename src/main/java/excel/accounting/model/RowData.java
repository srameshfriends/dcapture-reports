package excel.accounting.model;

import excel.accounting.db.QueryBuilder;

/**
 * Row Data
 */
public abstract class RowData {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNew() {
        return getId() < 1;
    }

    public abstract String createQuery();

    public abstract QueryBuilder updateQuery(String queryName);
}
