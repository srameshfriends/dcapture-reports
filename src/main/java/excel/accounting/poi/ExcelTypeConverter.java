package excel.accounting.poi;

import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

/**
 * Excel Type Converter
 * @author Ramesh
 * @since Oct, 2016
 */
public interface ExcelTypeConverter<T> {

    String[] getColumnNames();

    T getExcelType(String type, Cell[] cellArray);

    Object[] getExcelRow(String type, T dataType);
}
