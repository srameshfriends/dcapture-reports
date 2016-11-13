package excel.accounting.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In Clause Query
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ClauseQuery {
    private String query;
    private List<Object> parameterList;

    public ClauseQuery() {
        this("");
    }

    public ClauseQuery(String query) {
        this.query = query;
        parameterList = new ArrayList<>();
    }

    public void addParameter(Object parameters) {
        Collections.addAll(parameterList, parameters);
    }

    List<Object> getParameterList() {
        return parameterList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int index = parameterList.size();
        while (0 < index) {
            sb.append(",?");
            index = index - 1;
        }
        return sb.toString().replaceFirst(",", "");
    }
}
