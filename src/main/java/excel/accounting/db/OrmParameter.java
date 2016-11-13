package excel.accounting.db;

import java.sql.SQLType;

/**
 * OrmParameter
 */
public class OrmParameter {
    private final int index;
    private final Object parameter;
    private final SQLType sqlType;

    public OrmParameter(int index, Object parameter) {
        this.index = index;
        this.parameter = parameter;
        this.sqlType = null;
    }

    public OrmParameter(int index, Object parameter, SQLType sqlType) {
        this.index = index;
        this.parameter = parameter;
        this.sqlType = sqlType;
    }

    public int getIndex() {
        return index;
    }

    public Object getParameter() {
        return parameter;
    }

    public SQLType getSqlType() {
        return sqlType;
    }
}
