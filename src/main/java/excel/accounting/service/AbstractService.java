package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.shared.AbstractControl;

import java.sql.SQLException;

/**
 * Abstract Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public abstract class AbstractService extends AbstractControl {

    protected final Transaction createTransaction() {
        return new Transaction(getApplicationControl().getConnectionPool());
    }

    protected abstract String getSqlFileName();

    protected QueryBuilder getQueryBuilder(String queryName) {
        return getDataProcessor().getQueryBuilder(getSqlFileName(), queryName);
    }

    protected void executeBatch(Transaction transaction) {
        try {
            transaction.executeBatch();
        } catch (SQLException ex) {
            setMessage(ex.getErrorCode() + " : " + ex.getMessage());
        }
    }
}
