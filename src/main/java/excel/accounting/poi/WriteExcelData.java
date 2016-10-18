package excel.accounting.poi;

import excel.accounting.model.RowData;
import excel.accounting.shared.RowDataProvider;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Write Excel Data
 */
public class WriteExcelData<T> {
    private final File file;
    private final RowDataProvider<T> rowDataProvider;

    public WriteExcelData(File file, RowDataProvider<T> rowDataProvider) {
        this.file = file;
        this.rowDataProvider = rowDataProvider;
    }

    public void writeRowData(String name, List<T> dataList) {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet(name);
            int rowIndex = 0;
            for (T rowData : dataList) {
                Row row = sheet.createRow(rowIndex);
                String[] cellData = rowDataProvider.getCellData(rowData);
                if (cellData != null) {
                    addCell(row, cellData);
                }
                rowIndex += 1;
            }
            workbook.write(new FileOutputStream(file));
            workbook.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addCell(Row row, String[] cellDataArray) {
        int cellIndex = 0;
        for (String cellData : cellDataArray) {
            Cell cell = row.createCell(cellIndex);
            cell.setCellValue(cellData);
            cellIndex += 1;
        }
    }
}
