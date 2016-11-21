package excel.accounting.db;

import java.sql.SQLException;
import java.util.List;

/**
 * Sql Reader
 */
public interface SqlReadResponse {
    void onSqlResponse(SqlQuery sqlQuery, SqlMetaData[] mdArray, List<Object[]> dataArrayList);

    void onSqlError(SqlQuery sqlQuery, SQLException exception);
}
