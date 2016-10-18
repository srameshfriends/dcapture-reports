package excel.accounting.db;

import javax.management.AttributeList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Query Parameter
 */
public class QueryBuilder {
    private String name;
    private StringBuilder builder;
    private List<Object> parameters;

    public QueryBuilder() {
        builder = new StringBuilder();
        parameters = new ArrayList<>();
    }

    public QueryBuilder append(String queryText) {
        builder.append(queryText);
        return QueryBuilder.this;
    }

    public QueryBuilder addInteger(int integer) {
        parameters.add(integer);
        return QueryBuilder.this;
    }

    public QueryBuilder addDate(Date date) {
        parameters.add(date);
        return QueryBuilder.this;
    }

    public QueryBuilder addBigDecimal(BigDecimal bigDecimal) {
        parameters.add(bigDecimal == null ? BigDecimal.ZERO : bigDecimal);
        return QueryBuilder.this;
    }

    public QueryBuilder addString(String text) {
        parameters.add(text == null ? "" : text);
        return QueryBuilder.this;
    }

    public QueryBuilder add(boolean bool) {
        parameters.add(bool);
        return QueryBuilder.this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return builder.toString();
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return builder.toString().isEmpty() ? super.toString() : builder.toString();
    }
}
