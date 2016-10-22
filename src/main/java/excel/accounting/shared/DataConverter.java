package excel.accounting.shared;

import excel.accounting.entity.AccountType;
import excel.accounting.entity.Status;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DataConverter
 */
public class DataConverter {
    private static final Logger logger = Logger.getLogger(DataConverter.class);
    private static DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-mm-dd");

    public static int getInteger(Cell cell) {
        Double decimal = getDouble(cell);
        return decimal.intValue();
    }

    public static AccountType getAccountType(Object accountType) {
        return accountType == null ? null : getEnum(AccountType.class, accountType.toString());
    }

    public static Status getStatus(Object status) {
        return status == null ? Status.Drafted : getEnum(Status.class, status.toString());
    }

    private static <E extends Enum<E>> E getEnum(Class<E> enumClass, String name) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }
        return null;
    }

    public static String getString(Cell cell) {
        return cell.getStringCellValue();
    }

    public static double getDouble(Cell cell) {
        double decimal;
        if (CellType.NUMERIC.equals(cell.getCellTypeEnum())) {
            decimal = cell.getNumericCellValue();
        } else {
            decimal = parseDouble(cell.getStringCellValue());
        }
        return decimal;
    }

    public static BigDecimal getBigDecimal(Cell cell) {
        return new BigDecimal(getDouble(cell));
    }

    public static Date getDate(Cell cell) {
        return getDate(cell, defaultDateFormat);
    }

    private static Date getDate(Cell cell, DateFormat dateFormat) {
        if (CellType.NUMERIC.equals(cell.getCellTypeEnum())) {
            Double decimal = cell.getNumericCellValue();
            long dateTime = decimal.longValue();
            return dateTime == 0 ? null : new Date(dateTime);
        } else {
            try {
                String value = cell.getStringCellValue();
                if (value != null) {
                    return dateFormat.parse(value);
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
        return null;
    }

    public static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return 0;
    }
}
