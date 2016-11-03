package excel.accounting.db;

/**
 * Row Columns To Entity
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public interface RowColumnsToEntity<T> {
    T getEntity(String queryName, Object[] columns);
}
