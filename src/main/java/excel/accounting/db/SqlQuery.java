package excel.accounting.db;

import java.util.LinkedList;

/**
 * SqlQuery
 */
public class SqlQuery extends LinkedList<Object> {
    private int id;
    private String query;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
