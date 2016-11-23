package excel.accounting.shared;

import excel.accounting.db.*;

/**
 * Has Apps Control
 */
public abstract class AbstractControl {
    private ApplicationControl control;
    private DataReader dataReader;
    private SqlTableMap sqlTableMap;
    private SqlResultSet sqlResultSet;

    public final void setApplicationControl(ApplicationControl control) {
        this.control = control;
        dataReader = new DataReader(control.getDataProcessor());
        sqlTableMap = control.getSqlTableMap();
        sqlResultSet = new SqlResultSet(control.getConnectionPool());
    }

    protected final ApplicationControl getApplicationControl() {
        return control;
    }

    protected final DataProcessor getDataProcessor() {
        return control.getDataProcessor();
    }

    protected QueryTool createSqlBuilder(int pid) {
        return new QueryTool(getSqlTableMap().getSchema()).setId(pid);
    }

    protected QueryTool createSqlBuilder() {
        return new QueryTool(getSqlTableMap().getSchema());
    }

    protected SqlTableMap getSqlTableMap() {
        return sqlTableMap;
    }

    protected Object getBean(String name) {
        return control.getBean(name);
    }

    protected final DataReader getDataReader() {
        return dataReader;
    }

    protected void setMessage(String message) {
        control.setMessage(message);
    }

    protected SqlResultSet getSqlResultSet() {
        return sqlResultSet;
    }

    protected SqlForwardTool getSqlForwardTool() {
        return control.getSqlForwardTool();
    }
}
