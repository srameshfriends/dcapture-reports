package excel.accounting.db;

/**
 * Row Type Converter
 */
public interface RowTypeConverter<T> {
    T getRowType(QueryBuilder builder, Object[] objectArray);
}
