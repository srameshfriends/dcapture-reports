package excel.accounting.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL Builder
 */
public class SQLBuilder extends OrmQuery {
    private OrmProcessor processor;
    private List<String> selectList;
    private WhereQuery whereQuery;
    private StringBuilder fromBuilder, orderByBuilder, updateBuilder;
    private List<Object> updateParameterList;
    private String updateSchemaTable;

    public SQLBuilder() {
        selectList = new ArrayList<>();
        fromBuilder = new StringBuilder();
    }

    public SQLBuilder(OrmProcessor processor) {
        this();
        this.processor = processor;
        setSchema(processor.getSchema());
    }

    private WhereQuery getWhereQuery() {
        if (whereQuery == null) {
            whereQuery = new WhereQuery();
        }
        return whereQuery;
    }

    private StringBuilder getOrderByBuilder() {
        if (orderByBuilder == null) {
            orderByBuilder = new StringBuilder();
        }
        return orderByBuilder;
    }

    public SQLBuilder select(Class<?> selectTable) {
        OrmTable ormTable = processor.getTable(selectTable);
        selectList.addAll(ormTable.getColumnList().stream().map(OrmColumn::getName).collect(Collectors.toList()));
        fromBuilder.append(" ").append(processor.getSchema()).append(".").append(ormTable.getName());
        setEntity(selectTable);
        return SQLBuilder.this;
    }

    public SQLBuilder from(String tableWithSchema) {
        fromBuilder.append(" ").append(tableWithSchema);
        return SQLBuilder.this;
    }

    public SQLBuilder from(Class<?> tableClass) {
        OrmTable ormTable = processor.getTable(tableClass);
        fromBuilder.append(" ").append(getSchema()).append(".").append(ormTable.getName());
        return SQLBuilder.this;
    }

    public SQLBuilder select(String... columns) {
        Collections.addAll(selectList, columns);
        return SQLBuilder.this;
    }

    public void whereOrIn(String query, List<Object> parameters) {
        getWhereQuery().whereOrIn(query, parameters);
    }

    public void whereOrIn(String query, Object[] parameters) {
        getWhereQuery().whereOrIn(query, parameters);
    }

    public void whereAndIn(String query, List<Object> parameters) {
        getWhereQuery().whereOrIn(query, parameters);
    }

    public void whereAndIn(String query, Object[] parameters) {
        getWhereQuery().whereOrIn(query, parameters);
    }

    public void orderBy(String... columns) {
        for (String col : columns) {
            getOrderByBuilder().append(col).append(", ");
        }
    }

    private StringBuilder getUpdateBuilder() {
        if (updateBuilder == null) {
            updateBuilder = new StringBuilder();
        }
        return updateBuilder;
    }

    private List<Object> getUpdateParameters() {
        if (updateParameterList == null) {
            updateParameterList = new ArrayList<>();
        }
        return updateParameterList;
    }

    public void updateColumn(String column, Object parameter) {
        getUpdateBuilder().append(column).append(" = ?,");
        getUpdateParameters().add(parameter);
    }

    public void update(String tableWithSchema) {
        updateSchemaTable = tableWithSchema;
    }

    public String getUpdateQuery() {
        return updateBuilder.substring(0, updateBuilder.length() - 1);
    }

    @Override
    public List<OrmParameter> getParameterList() {
        List<OrmParameter> parameterList = new ArrayList<>();
        if (whereQuery != null) {
            int index = 0;
            for (Object obj : whereQuery.getParameterList()) {
                index += 1;
                parameterList.add(new OrmParameter(index, obj));
            }
        }
        super.setParameterList(parameterList);
        return super.getParameterList();
    }

    @Override
    public String getQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        for (String sel : selectList) {
            sb.append(sel).append(",");
        }
        sb.replace(sb.toString().length() - 1, sb.toString().length(), " ");
        sb.append(" from ").append(fromBuilder.toString()).append(" ");
        if (whereQuery != null) {
            sb.append(whereQuery.toString());
        }
        if (orderByBuilder != null) {
            orderByBuilder.replace(orderByBuilder.length() - 2, orderByBuilder.length(), " ");
            sb.append(" order by ").append(orderByBuilder.toString());
        }
        sb.append(";");
        super.setQuery(sb.toString());
        return super.getQuery();
    }
}
