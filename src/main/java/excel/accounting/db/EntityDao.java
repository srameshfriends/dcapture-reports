package excel.accounting.db;

import java.util.List;

/**
 * Sql Entity Dao
 */
public interface EntityDao<T> {
    void onEntityResult(int pid, List<T> dataList);

    void onEntityError(int pid, Exception ex);
}
