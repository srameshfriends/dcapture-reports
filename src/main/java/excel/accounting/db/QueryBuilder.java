package excel.accounting.db;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;

/**
 * Query Parameter
 */
public class QueryBuilder {
    private final String queryName, queryTemplate;
    private StringBuilder selectBuilder, joinBuilder, whereBuilder, orderByBuilder;
    private String limitQuery;
    private Map<Integer, Object> parameters;

    QueryBuilder(String queryName, String queryTemplate) {
        this.queryName = queryName;
        this.queryTemplate = queryTemplate;
        parameters = new HashMap<>();
    }

    public String getQueryName() {
        return queryName;
    }

    public String getQueryTemplate() {
        return queryTemplate;
    }

    private void appendSelect(String txt) {
        if (selectBuilder == null) {
            selectBuilder = new StringBuilder();
        }
        selectBuilder.append(txt);
    }

    private void appendJoin(String txt) {
        if (joinBuilder == null) {
            joinBuilder = new StringBuilder();
        }
        joinBuilder.append(txt);
    }

    private void appendOrderBy(String txt) {
        if (orderByBuilder == null) {
            orderByBuilder = new StringBuilder();
        }
        orderByBuilder.append(txt);
    }

    private void appendWhere(String txt) {
        if (whereBuilder == null) {
            whereBuilder = new StringBuilder();
        }
        whereBuilder.append(txt);
    }

    public QueryBuilder select(String query) {
        appendSelect(query);
        return QueryBuilder.this;
    }

    public QueryBuilder where(String query) {
        appendWhere(query);
        return QueryBuilder.this;
    }

    public QueryBuilder orderBy(String query) {
        appendOrderBy(query);
        return QueryBuilder.this;
    }

    public QueryBuilder limit(int startFrom, int rowCount) {
        limitQuery = " limit " + rowCount + " offset " + startFrom;
        return QueryBuilder.this;
    }

    public QueryBuilder add(int index, Object parameter) {
        parameters.put(index, parameter);
        return QueryBuilder.this;
    }

    public Map<Integer, Object> getParameters() {
        return parameters;
    }

    public String getQuery() {
        String query = getQueryTemplate();
        if (query.contains("$select") && selectBuilder != null) {
            query = StringUtils.replace(query, "$select", selectBuilder.toString());
        }
        if (query.contains("$where") && whereBuilder != null) {
            query = StringUtils.replace(query, "$where", whereBuilder.toString());
        }
        if (query.contains("$join") && joinBuilder != null) {
            query = StringUtils.replace(query, "$where", joinBuilder.toString());
        }
        if (query.contains("$orderby") && orderByBuilder != null) {
            query = StringUtils.replace(query, "$orderby", orderByBuilder.toString());
        }
        if (limitQuery != null) {
            query = query.concat(limitQuery);
        }
        return query;
    }

    @Override
    public int hashCode() {
        return queryName.hashCode();
    }

    @Override
    public String toString() {
        return queryName;
    }
}
