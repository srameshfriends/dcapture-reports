package excel.accounting.shared;

import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

/**
 * Row Data Provider
 */
public interface RowDataProvider<T> {
    T getRowData(int rowIndex, List<Cell> cellList);

    T getRowData(String queryName, Object[] objectArray);

    String[] getCellData(T rowData);
}
