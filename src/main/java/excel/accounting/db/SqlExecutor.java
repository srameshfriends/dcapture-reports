package excel.accounting.db;

/**
 * Sql Executor
 */
public interface SqlExecutor {
    void onSqlCompleted(int processId, Object result);

    void onSqlError(int processId, Exception ex);
}
