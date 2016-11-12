package excel.accounting.db;

import java.util.List;

/**
 * Orm Query
 */
public class OrmQuery {
    private String query;
    private List<OrmParameter> parameterList;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<OrmParameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<OrmParameter> parameterList) {
        this.parameterList = parameterList;
    }
}
