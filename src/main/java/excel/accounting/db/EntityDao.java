package excel.accounting.db;

import java.util.List;

/**
 * Sql Entity Dao
 */
public interface EntityDao<T> {
    void onEntityDaoCompleted(int pid, List<T> dataList);

    void onEntityDaoError(int pid, Exception ex);
}
