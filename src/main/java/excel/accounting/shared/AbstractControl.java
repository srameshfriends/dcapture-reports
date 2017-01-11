package excel.accounting.shared;

import excel.accounting.db.*;

/**
 * Abstract Control
 */
public abstract class AbstractControl {
    private ApplicationControl control;

    public final void setApplicationControl(ApplicationControl control) {
        this.control = control;
    }

    protected final ApplicationControl getApplicationControl() {
        return control;
    }

    protected Object getBean(String name) {
        return control.getBean(name);
    }

    protected void setMessage(String message) {
        control.setMessage(message);
    }

    protected SqlProcessor getSqlProcessor() {
        return control.getSqlProcessor();
    }

    protected SqlReader getSqlReader() {
        return control.getSqlProcessor().getSqlReader();
    }
}
