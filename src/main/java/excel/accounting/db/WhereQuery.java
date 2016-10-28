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
    private List<Object> parameterList;

    public WhereQuery(Object... parameters) {
        parameterList = new ArrayList<>();
        add(parameters);
    }

    public void add(Object... parameters) {
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