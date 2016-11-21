package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.shared.AbstractControl;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Abstract Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public abstract class AbstractService extends AbstractControl {

    @Deprecated
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

    protected void executeBatch(int pid, List<SqlQuery> queryList, SqlWriteResponse response) {
        SqlTransaction transaction = new SqlTransaction(getApplicationControl().getConnectionPool());
        transaction.setResponse(response);
        transaction.setProcessId(pid);
        transaction.setDoBatchUpdate(true);
        transaction.addAll(queryList);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(transaction);
    }

    protected void executeCommit(int pid, List<SqlQuery> queryList, SqlWriteResponse response) {
        SqlTransaction transaction = new SqlTransaction(getApplicationControl().getConnectionPool());
        transaction.setResponse(response);
        transaction.setProcessId(pid);
        transaction.addAll(queryList);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(transaction);
    }

    protected void insert(int pid, List<?> dataList, SqlWriteResponse response) {
        SqlTransaction transaction = new SqlTransaction(getApplicationControl().getConnectionPool());
        transaction.setResponse(response);
        transaction.setProcessId(pid);
        SqlForwardTool forwardTool = getApplicationControl().getSqlForwardTool();
        transaction.addAll(dataList.stream().map(forwardTool::insertQuery).collect(Collectors.toList()));
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(transaction);
    }
}
