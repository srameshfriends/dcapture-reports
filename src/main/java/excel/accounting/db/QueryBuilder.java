package excel.accounting.db;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Query Parameter
 */
public class QueryBuilder {
    private StringBuilder selectBuilder, joinBuilder, whereBuilder, orderByBuilder;
    private String queryName, queryTemplate, limitQuery;
    private Map<Integer, Object> parameters;
    private Map<String, String> replaceMap;

    public QueryBuilder() {
        parameters = new HashMap<>();
        replaceMap = new HashMap<>();
        queryName = "";
    }

    QueryBuilder(String name, String queryTemplate) {
        this();
        queryName = name;
        setQueryTemplate(queryTemplate);
    }

    public void setQueryTemplate(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }

    public String getQueryName() {
        return queryName;
    }

    private void appendSelect(String... fields) {
        if (selectBuilder == null) {
            selectBuilder = new StringBuilder();
        }
        for(String fld : fields) {
            selectBuilder.append(fld).append(",");
        }
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

    public QueryBuilder addInClauseQuery(String replaceName, InClauseQuery inClauseQuery) {
        replaceMap.put(replaceName, inClauseQuery == null ? "" : inClauseQuery.toString());
        if(inClauseQuery != null) {
            inClauseQuery.getParameterList().forEach(this::addParameter);
        }
        return QueryBuilder.this;
    }

    public QueryBuilder addSearchTextQuery(String replaceName, SearchTextQuery searchTextQuery) {
        if (searchTextQuery != null) {
            replaceMap.put(replaceName, searchTextQuery.toString());
            searchTextQuery.getParameterList().forEach(this::addParameter);
        } else {
            replaceMap.put(replaceName, "");
        }
        return QueryBuilder.this;
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

    public QueryBuilder addParameter(Object parameter) {
        add(parameters.size() + 1, parameter);
        return QueryBuilder.this;
    }

    Map<Integer, Object> getParameters() {
        return parameters;
    }

    public String getQuery() {
        String query = queryTemplate;
        if (query.contains("$select") && selectBuilder != null) {
            String temp = StringUtils.removeEnd(selectBuilder.toString(), ",");
            query = StringUtils.replace(query, "$select", temp);
        }
        if (query.contains("$where") && whereBuilder != null) {
            query = StringUtils.replace(query, "$where", whereBuilder.toString());
        }
        if (query.contains("$join") && joinBuilder != null) {
            query = StringUtils.replace(query, "$join", joinBuilder.toString());
        }
        if (query.contains("$orderby") && orderByBuilder != null) {
            query = StringUtils.replace(query, "$orderby", orderByBuilder.toString());
        }
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            query = StringUtils.replace(query, entry.getKey().toLowerCase(), entry.getValue());
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
