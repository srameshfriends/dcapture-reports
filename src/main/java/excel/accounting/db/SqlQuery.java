package excel.accounting.db;

import java.util.LinkedList;
import java.util.List;

/**
 * SqlQuery
 */
public class SqlQuery {
    private String query;
    private LinkedList<Object> parameterList;

    public SqlQuery() {
        this("Database query has not found");
    }

    public SqlQuery(String query) {
        parameterList = new LinkedList<>();
        setQuery(query);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void add(Object parameter) {
        parameterList.add(parameter);
    }

    public void addAll(List<Object> parameters) {
        parameterList.addAll(parameters);
    }

    public LinkedList<Object> getParameterList() {
        return parameterList;
    }

    @Override
    public String toString() {
        return query;
    }
}
