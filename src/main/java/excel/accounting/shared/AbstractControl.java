package excel.accounting.shared;

import excel.accounting.db.*;

/**
 * Has Apps Control
 */
public abstract class AbstractControl {
    private ApplicationControl control;
    private DataReader dataReader;
    private SqlTableMap sqlTableMap;

    public final void setApplicationControl(ApplicationControl control) {
        this.control = control;
        dataReader = new DataReader(control.getDataProcessor());
        sqlTableMap = control.getSqlTableMap();
    }

    protected final ApplicationControl getApplicationControl() {
        return control;
    }

    protected final DataProcessor getDataProcessor() {
        return control.getDataProcessor();
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
}
