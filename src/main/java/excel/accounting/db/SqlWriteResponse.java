package excel.accounting.db;

import java.sql.SQLException;
import java.util.List;

/**
 * Sql Writer
 */
public interface SqlWriteResponse {
    void onSqlResponse(int processId);

    void onSqlError(int processId, SQLException exception);
}
