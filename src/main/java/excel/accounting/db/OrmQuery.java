package excel.accounting.db;

import java.util.List;

/**
 * Orm Query
 */
public class OrmQuery {
    private Class<?> entity;
    private String schema, query;
    private List<OrmParameter> parameterList;

    public Class<?> getEntity() {
        return entity;
    }

    public void setEntity(Class<?> entity) {
        this.entity = entity;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

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
