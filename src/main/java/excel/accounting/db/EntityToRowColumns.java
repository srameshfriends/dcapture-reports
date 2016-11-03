package excel.accounting.db;

import java.util.Map;

/**
 * Entity To Row Columns
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public interface EntityToRowColumns<T> {

    Map<Integer, Object> getColumnsMap(final String queryName, T entity);
}
