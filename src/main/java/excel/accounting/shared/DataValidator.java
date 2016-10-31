package excel.accounting.shared;

import java.math.BigDecimal;

/**
 * Data Validator
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class DataValidator {

    public static boolean isMoreThenZero(BigDecimal bigDecimal) {
        return bigDecimal != null && 0 > BigDecimal.ZERO.compareTo(bigDecimal);
    }
}
