package excel.accounting.ui;

/**
 * Table Row Handler
 */
public interface TableRowHandler<T> {
    void onTableRowModified(T value, String name, Object newValue);
}
