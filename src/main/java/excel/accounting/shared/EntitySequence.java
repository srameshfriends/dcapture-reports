package excel.accounting.shared;

/**
 * Entity Sequence
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public abstract class EntitySequence {
    private static String getSequence(String prefix, int sequence) {
        String value = "";
        if (10 > sequence) {
            value = "000";
        } else if (9 < sequence && 100 > sequence) {
            value = "00";
        } else if (99 < sequence && 1000 > sequence) {
            value = "0";
        } else if (999 < sequence && 10000 > sequence) {
            value = "";
        }
        return prefix + value + sequence;
    }

    public static String getExpenseItemCode(int index) {
        return getSequence("EI", index);
    }

    public static String getExchangeRateCode(int index) {
        return getSequence("ER", index);
    }
}

