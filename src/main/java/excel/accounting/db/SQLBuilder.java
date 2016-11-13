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
    private StringBuilder fromBuilder;

    public SQLBuilder() {
        selectList = new ArrayList<>();
        fromBuilder = new StringBuilder();
    }

    public SQLBuilder(OrmProcessor processor) {
        this();
        this.processor = processor;
    }

    private WhereQuery getWhereQuery() {
        if (whereQuery == null) {
            whereQuery = new WhereQuery();
        }
        return whereQuery;
    }

    public void select(Class<?> selectTable) {
        OrmTable ormTable = processor.getTable(selectTable);
        selectList.addAll(ormTable.getColumnList().stream().map(OrmColumn::getName).collect(Collectors.toList()));
        fromBuilder.append(" ").append(processor.getSchema()).append(".").append(ormTable.getName());
    }

    public void select(String... columns) {
        Collections.addAll(selectList, columns);
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
        setQuery(sb.toString());
        return super.getQuery();
    }
}
