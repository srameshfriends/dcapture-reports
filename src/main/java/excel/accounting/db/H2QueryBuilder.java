package excel.accounting.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Query Builder
 */
public class H2QueryBuilder implements QueryBuilder {
    private final String schema;
    private String selectTable, updateTable, deleteTable, insertTable;
    private List<String> selectColumns, updateColumns, insertColumns, orderByColumns;
    private WhereQuery whereQuery;
    private List<Object> updateParameters, insertParameters;
    private StringBuilder joinBuilder;
    private SqlQuery sqlQuery;
    private int limit = -1, offset = -1;

    H2QueryBuilder(String schema) {
        this.schema = schema;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public H2QueryBuilder updateColumns(String column, Object object) {
        getUpdateColumns().add(column);
        getUpdateParameters().add(object);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder update(String tableName) {
        this.updateTable = tableName;
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder deleteFrom(String table) {
        deleteTable = table;
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder insertInto(String tableName) {
        this.insertTable = tableName;
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder insertColumns(String column, Object object) {
        getInsertColumns().add(column);
        getInsertParameters().add(object);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder join(String joinQuery) {
        getJoinBuilder().append(joinQuery);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder selectColumns(String... columns) {
        for (String col : columns) {
            getSelectColumns().add(col);
        }
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder selectColumns(Set<String> columnSet) {
        getSelectColumns().addAll(columnSet);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder selectFrom(String table) {
        selectTable = table;
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder where(String column, Object parameter) {
        getWhereQuery().where(column, parameter);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder where(SearchTextQuery searchTextQuery) {
        getWhereQuery().where(searchTextQuery);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder whereOrIn(String query, List<Object> parameters) {
        getWhereQuery().whereOrIn(query, parameters);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder whereOrIn(String query, Object[] parameters) {
        getWhereQuery().whereOrIn(query, parameters);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder whereAndIn(String query, List<Object> parameters) {
        getWhereQuery().whereOrIn(query, parameters);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder whereAndIn(String query, Object[] parameters) {
        getWhereQuery().whereOrIn(query, parameters);
        return H2QueryBuilder.this;
    }

    @Override
    public H2QueryBuilder orderBy(String... columns) {
        for (String col : columns) {
            getOrderByColumns().add(col);
        }
        return H2QueryBuilder.this;
    }

    @Override
    public QueryBuilder limit(int limit) {
        this.limit = limit;
        return H2QueryBuilder.this;
    }

    @Override
    public QueryBuilder limitOffset(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
        return H2QueryBuilder.this;
    }

    @Override
    public SqlQuery getSqlQuery() {
        if (sqlQuery == null) {
            sqlQuery = new SqlQuery();
            if (selectTable != null) {
                buildSelectQuery(sqlQuery);
            } else if (updateTable != null) {
                buildUpdateQuery(sqlQuery);
            } else if (insertTable != null) {
                buildInsertQuery(sqlQuery);
            } else if (deleteTable != null) {
                buildDeleteQuery(sqlQuery);
            }
        }
        return sqlQuery;
    }

    private void buildSelectQuery(SqlQuery sqlQuery) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (String sel : selectColumns) {
            sb.append(sel).append(",");
        }
        sb.replace(sb.toString().length() - 1, sb.toString().length(), " ");
        sb.append(" from ").append(getSchema()).append(".").append(selectTable);
        if (joinBuilder != null) {
            sb.append(" ").append(joinBuilder.toString());
        }
        if (whereQuery != null) {
            sb.append(" ").append(whereQuery.toString());
            sqlQuery.addAll(whereQuery.getParameterList());
        }
        if (orderByColumns != null) {
            StringBuilder orderByBuild = new StringBuilder();
            for (String orderBy : orderByColumns) {
                orderByBuild.append(" order by ").append(orderBy).append(",");
            }
            orderByBuild.replace(orderByBuild.length() - 1, orderByBuild.length(), " ");
            sb.append(orderByBuild.toString());
        }
        if (0 < limit) {
            sb.append(" limit ").append(limit);
        }
        if (0 < offset) {
            sb.append(" offset ").append(offset);
        }
        sb.append(";");
        sqlQuery.setQuery(sb.toString());
    }

    private void buildUpdateQuery(SqlQuery sqlQuery) {
        sqlQuery.addAll(getUpdateParameters());
        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(getSchema()).append(".").append(updateTable).append(" set ");
        for (String upd : updateColumns) {
            sb.append(upd).append(" = ?,");
        }
        sb.replace(sb.toString().length() - 1, sb.toString().length(), " ");
        if (joinBuilder != null) {
            sb.append(" ").append(joinBuilder.toString());
        }
        if (whereQuery != null) {
            sb.append(" ").append(whereQuery.toString());
            sqlQuery.addAll(whereQuery.getParameterList());
        }
        sb.append(";");
        sqlQuery.setQuery(sb.toString());
    }

    private void buildInsertQuery(SqlQuery sqlQuery) {
        sqlQuery.addAll(getInsertParameters());
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(getSchema()).append(".").append(insertTable).append(" (");
        StringBuilder pss = new StringBuilder("(");
        for (String ic : insertColumns) {
            sb.append(ic).append(",");
            pss.append("?,");
        }
        sb.replace(sb.toString().length() - 1, sb.toString().length(), ")");
        pss.replace(pss.toString().length() - 1, pss.toString().length(), ")");
        sb.append(" values ").append(pss.toString()).append(";");
        sqlQuery.setQuery(sb.toString());
    }

    private void buildDeleteQuery(SqlQuery sqlQuery) {
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ").append(getSchema()).append(".").append(deleteTable);
        if (joinBuilder != null) {
            sb.append(" ").append(joinBuilder.toString());
        }
        if (whereQuery != null) {
            sb.append(" ").append(whereQuery.toString());
            sqlQuery.addAll(whereQuery.getParameterList());
        }
        sb.append(";");
        sqlQuery.setQuery(sb.toString());
    }

    private List<String> getSelectColumns() {
        if (selectColumns == null) {
            selectColumns = new ArrayList<>();
        }
        return selectColumns;
    }

    private List<String> getUpdateColumns() {
        if (updateColumns == null) {
            updateColumns = new ArrayList<>();
        }
        return updateColumns;
    }

    private List<Object> getUpdateParameters() {
        if (updateParameters == null) {
            updateParameters = new ArrayList<>();
        }
        return updateParameters;
    }

    private List<String> getInsertColumns() {
        if (insertColumns == null) {
            insertColumns = new ArrayList<>();
        }
        return insertColumns;
    }

    private List<Object> getInsertParameters() {
        if (insertParameters == null) {
            insertParameters = new ArrayList<>();
        }
        return insertParameters;
    }

    private StringBuilder getJoinBuilder() {
        if (joinBuilder == null) {
            joinBuilder = new StringBuilder();
        }
        return joinBuilder;
    }

    private List<String> getOrderByColumns() {
        if (orderByColumns == null) {
            orderByColumns = new ArrayList<>();
        }
        return orderByColumns;
    }

    private WhereQuery getWhereQuery() {
        if (whereQuery == null) {
            whereQuery = new WhereQuery();
        }
        return whereQuery;
    }
}
