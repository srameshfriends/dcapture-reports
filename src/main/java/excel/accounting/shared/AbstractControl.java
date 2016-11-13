package excel.accounting.shared;

import excel.accounting.db.DataProcessor;
import excel.accounting.db.DataReader;

/**
 * Has Apps Control
 */
public abstract class AbstractControl {
    private ApplicationControl control;
    private DataReader dataReader;

    public final void setApplicationControl(ApplicationControl control) {
        this.control = control;
        dataReader = new DataReader(control.getDataProcessor());
    }

    protected final ApplicationControl getApplicationControl() {
        return control;
    }

    protected final DataProcessor getDataProcessor() {
        return control.getDataProcessor();
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
