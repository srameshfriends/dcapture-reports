package excel.accounting.shared;

import excel.accounting.db.DataProcessor;
import excel.accounting.db.DataReader;
import excel.accounting.db.OrmReader;
import excel.accounting.db.SQLBuilder;

/**
 * Has Apps Control
 */
public abstract class AbstractControl {
    private ApplicationControl control;
    private DataReader dataReader;
    private OrmReader ormReader;

    public final void setApplicationControl(ApplicationControl control) {
        this.control = control;
        dataReader = new DataReader(control.getDataProcessor());
        ormReader = new OrmReader();
        ormReader.setProcessor(getApplicationControl().getOrmProcessor());
    }

    protected final ApplicationControl getApplicationControl() {
        return control;
    }

    protected final DataProcessor getDataProcessor() {
        return control.getDataProcessor();
    }

    protected SQLBuilder createSQLQuery() {
        return new SQLBuilder(getApplicationControl().getOrmProcessor());
    }

    protected Object getBean(String name) {
        return control.getBean(name);
    }

    protected final DataReader getDataReader() {
        return dataReader;
    }

    public final OrmReader getOrmReader() {
        return ormReader;
    }

    protected void setMessage(String message) {
        control.setMessage(message);
    }
}
