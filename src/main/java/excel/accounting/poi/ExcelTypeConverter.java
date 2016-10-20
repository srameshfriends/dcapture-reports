package excel.accounting.poi;

import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

/**
 * Excel Type Converter
 * @author Ramesh
 * @since Oct, 2016
 */
public interface ExcelTypeConverter<T> {

    T getExcelType(int rowIndex, List<Cell> cellList);

    Object[] getExcelRow(T dataType);
}
