package excel.accounting.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Where Query
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class WhereQuery {
    private List<String> queryList;
    private List<Object> parameterList;

    WhereQuery() {
        parameterList = new ArrayList<>();
        queryList = new ArrayList<>();
    }

    public void whereAndEqual(String query, Object parameter) {
        queryList.add(" and " + query + " = ?");
        parameterList.add(parameter);
    }

    public void whereOrEqual(String query, Object parameter) {
        queryList.add(" or " + query + " = ?");
        parameterList.add(parameter);
    }

    public void whereOrIn(String query, Object[] parameters) {
        queryList.add(" or " + query + " in " + buildInArray(parameters.length));
        Collections.addAll(parameterList, parameters);
    }

    public void whereOrIn(String query, List<Object> parameters) {
        queryList.add(" or " + query + " in " + buildInArray(parameters.size()));
        parameterList.addAll(parameters);
    }

    public void whereAndIn(String query, Object[] parameters) {
        queryList.add(" and " + query + " in " + buildInArray(parameters.length));
        Collections.addAll(parameterList, parameters);
    }

    public void whereAndIn(String query, List<Object> parameters) {
        queryList.add(" and " + query + " in " + buildInArray(parameters.size()));
        parameterList.addAll(parameters);
    }

    private String buildInArray(int length) {
        StringBuilder sb = new StringBuilder("(");
        while (0 < length) {
            sb.append(",?");
            length = length - 1;
        }
        return sb.toString().replaceFirst(",", ")");
    }

    List<Object> getParameterList() {
        return parameterList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String query : queryList) {
            sb.append(query);
        }
        String query = sb.toString();
        if (query.startsWith(" and ") || query.startsWith(" or ")) {
            query = query.replaceFirst(" and ", " where ");
        }
        return query;
    }
}
