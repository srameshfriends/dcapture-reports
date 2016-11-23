package excel.accounting.db;

import java.sql.SQLException;

/**
 * Sql Error
 */
interface SqlError {
    void onSqlError(int pid, SQLException ex);
}
