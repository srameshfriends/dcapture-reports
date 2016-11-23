package excel.accounting.db;

/**
 * Sql Write
 */
public interface SqlWriter extends SqlError {
    void onSqlUpdated(int pid);
}
