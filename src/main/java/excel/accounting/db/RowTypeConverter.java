package excel.accounting.db;

import java.util.Map;

/**
 * Row Type Converter
 */
public interface RowTypeConverter<T> {
    T getRowType(QueryBuilder builder, Object[] objectArray);

    Map<Integer, Object> getRowObjectMap(QueryBuilder builder, T type);
}
