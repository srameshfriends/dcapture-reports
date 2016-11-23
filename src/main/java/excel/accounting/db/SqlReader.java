package excel.accounting.db;

import java.util.List;

/**
 * Sql Reader
 */
public interface SqlReader extends SqlError {
    void onSqlResult(int pid, SqlMetaData[] mdArray, List<Object[]> dataArrayList);
}
