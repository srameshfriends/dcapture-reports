package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.shared.HasAppsControl;

import java.sql.SQLException;

/**
 * Abstract Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public abstract class AbstractService implements HasAppsControl, HasDataProcessor {
    private ApplicationControl applicationControl;
    private DataReader dataReader;

    @Override
    public void setApplicationControl(ApplicationControl control) {
        this.applicationControl = control;
        dataReader = new DataReader(applicationControl.getDataProcessor());
    }

    protected void setMessage(String message) {
        applicationControl.setMessage(message);
    }

    @Override
    public DataReader getDataReader() {
        return dataReader;
    }

    @Override
    public Transaction createTransaction() {
        return new Transaction(applicationControl.getDataProcessor());
    }

    protected abstract String getSqlFileName();

    protected QueryBuilder getQueryBuilder(String queryName) {
        return applicationControl.getDataProcessor().getQueryBuilder(getSqlFileName(), queryName);
    }

    protected ApplicationControl getApplicationControl() {
        return applicationControl;
    }

    protected Object getBean(String name) {
        return applicationControl.getBean(name);
    }

    protected void executeBatch(Transaction transaction) {
        try {
            transaction.executeBatch();
        } catch (SQLException ex) {
            setMessage(ex.getErrorCode() + " : " + ex.getMessage());
        }
    }
}
