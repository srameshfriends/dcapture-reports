package dcapture.reports.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

public class CurrencyFormat {
    private final DecimalFormat decimalFormat = new DecimalFormat("###,###,###.00");
    private boolean isMinusPrefix, isZeroEmpty, isIndianCurrency;

    public CurrencyFormat() {
        this.isZeroEmpty = true;
        this.isMinusPrefix = false;
        this.isIndianCurrency = true;
    }

    public CurrencyFormat(boolean isMinusPrefix, boolean isZeroEmpty) {
        this.isMinusPrefix = isMinusPrefix;
        this.isZeroEmpty = isZeroEmpty;
        this.isIndianCurrency = true;
    }

    public void setMinusPrefix(boolean minusPrefix) {
        isMinusPrefix = minusPrefix;
    }

    public void setZeroEmpty(boolean zeroEmpty) {
        isZeroEmpty = zeroEmpty;
    }

    public void setIndianCurrency(boolean indianCurrency) {
        isIndianCurrency = indianCurrency;
    }

    public String format(int value) {
        if (0 == value) {
            return isZeroEmpty ? "" : "0";
        }
        if (!isIndianCurrency) {
            return decimalFormat.format(value);
        }
        return toFormattedText(value + "", 0 > value);
    }

    public String format(double value) {
        if (0 == value) {
            return isZeroEmpty ? "" : "0";
        }
        if (!isIndianCurrency) {
            return decimalFormat.format(value);
        }
        return toFormattedText(value + "", 0 > value);
    }

    public String format(float value) {
        if (0 == value) {
            return isZeroEmpty ? "" : "0";
        }
        if (!isIndianCurrency) {
            return decimalFormat.format(value);
        }
        return toFormattedText(value + "", 0 > value);
    }

    public String format(BigDecimal value) {
        if (value == null) {
            return isZeroEmpty ? "" : null;
        }
        if (BigDecimal.ZERO.equals(value)) {
            return isZeroEmpty ? "" : "0";
        }
        if (!isIndianCurrency) {
            return decimalFormat.format(value);
        }
        return toFormattedText(value.toString(), 0 < BigDecimal.ZERO.compareTo(value));
    }

    public Number parse(String text) {
        if (text == null) {
            return 0;
        }
        text = text.replaceAll("\\s+", "");
        if (text.isBlank()) {
            return 0;
        }
        if (!isIndianCurrency) {
            try {
                return decimalFormat.parse(text);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        char chr = text.charAt(0);
        if (!Character.isDigit(text.charAt(0)) && chr != '.' && chr != '(' && chr != '-') {
            text = text.substring(1);
        }
        chr = text.charAt(text.length() - 1);
        if (!Character.isDigit(text.charAt(text.length() - 1)) && chr != '.' && chr != ')' && chr != '-') {
            text = text.substring(0, text.length() - 1);
        }
        if (text.isBlank()) {
            return 0;
        }
        boolean isTemp = text.startsWith("-");
        if (!isTemp) {
            isTemp = text.startsWith("(");
        }
        final boolean isNegative = isTemp;
        text = text.replace("(", "");
        text = text.replace(")", "");
        text = text.replace("-", "");
        text = text.replaceAll(",", "");
        if (text.isBlank()) {
            return 0;
        }
        BigDecimal number = new BigDecimal(text);
        return isNegative ? number.negate() : number;
    }

    public int parseInt(String text) {
        return parse(text).intValue();
    }

    public int parseInt(String text, int defaultValue) {
        int value = parse(text).intValue();
        return 0 == value ? defaultValue : value;
    }

    public double parseDouble(String text) {
        return parse(text).doubleValue();
    }

    public long parseLong(String text) {
        return parse(text).longValue();
    }

    public BigDecimal parseBigDecimal(String text) {
        return BigDecimal.valueOf(parse(text).doubleValue());
    }

    private String toFormattedText(String text, boolean isNegative) {
        int dotIndex = text.indexOf(".");
        String afterDot = 0 > dotIndex ? "" : text.substring(dotIndex + 1);
        String beforeDot = 0 > dotIndex ? text : text.substring(0, dotIndex);
        if (isNegative) {
            beforeDot = beforeDot.substring(1);
        }
        StringBuilder builder = new StringBuilder();
        int charIndex = 0;
        for (int idx = beforeDot.length() - 1; idx >= 0; idx--) {
            char ch = beforeDot.charAt(idx);
            builder.insert(0, ch);
            charIndex += 1;
            if (3 == charIndex) {
                builder.insert(0, ",");
            } else if (3 < charIndex && 1 == (charIndex % 2)) {
                builder.insert(0, ",");
            }
        }
        text = builder.toString();
        if (text.startsWith(",")) {
            text = text.substring(1);
        }
        if (0 < afterDot.length()) {
            text = text + "." + afterDot;
        }
        if (isNegative) {
            return isMinusPrefix ? "-" + text : "(" + text + ")";
        }
        return text;
    }
}