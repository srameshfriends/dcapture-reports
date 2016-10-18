package excel.accounting.shared;

import org.apache.poi.ss.usermodel.Cell;

/**
 * DataConverter
 */
public class DataConverter {
    public static int getInteger(Cell cell) {
        String value = cell.getStringCellValue();
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return 0;
    }
}
