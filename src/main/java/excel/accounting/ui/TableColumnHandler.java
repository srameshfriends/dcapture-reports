package excel.accounting.ui;

/**
 * Row Modified Handler
 */
public interface TableColumnHandler {
    void onEditableRowEvent(int rowIndex, String name, Object oldValue, Object newValue);
}
