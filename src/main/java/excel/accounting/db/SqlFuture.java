package excel.accounting.db;

/**
 * Sql Feature
 */
public interface SqlFuture extends SqlError {
    default void onSqlFuture(SqlMetaDataResult dataResult) {
    }
}
