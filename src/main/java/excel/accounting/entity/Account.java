package excel.accounting.entity;

import excel.accounting.db.QueryBuilder;
import excel.accounting.model.RowData;

/**
 * Account
 */
public class Account extends RowData {
    private String code, name;

    @Override
    public String createQuery() {
        return "CREATE TABLE IF NOT EXISTS entity.account (" +
                "id INTEGER auto_increment," +
                "code VARCHAR(64) NOT NULL," +
                "name VARCHAR(256) NOT NULL," +
                "PRIMARY KEY (id))";
    }

    private QueryBuilder insertQuery() {
        QueryBuilder builder = new QueryBuilder();
        builder.append("INSERT INTO entity.account (code, name)  VALUES(?,?)");
        builder.addString(getCode());
        builder.addString(getName());
        return builder;
    }

    @Override
    public QueryBuilder updateQuery(String queryType) {
        if (isNew()) {
            return insertQuery();
        }
        QueryBuilder builder = new QueryBuilder();
        builder.append("UPDATE entity.account SET code = ?, name = ? WHERE id = ?");
        builder.addString(getCode()).addString(getName()).addInteger(getId());
        return builder;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
