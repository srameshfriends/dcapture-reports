package excel.accounting.shared;

import org.apache.commons.lang3.StringUtils;

/**
 * String Rules
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class StringRules {
    private int minLength, maxLength;
    private RulesType rulesType;

    public void setMinMaxLength(int minLength, int maxLength) {
        if (0 > minLength) {
            minLength = 0;
        }
        if (0 > maxLength) {
            maxLength = 0;
        }
        if (minLength > maxLength) {
            int temp = minLength;
            minLength = maxLength;
            maxLength = temp;
        }
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public void setRulesType(RulesType rulesType) {
        this.rulesType = rulesType;
    }

    public String getMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Empty text are not allowed";
        }
        text = text.trim();
        if (0 < minLength && minLength > text.length()) {
            return "Minimum " + minLength + " chars are required";
        }
        if (0 < maxLength && maxLength < text.length()) {
            return "Maximum " + maxLength + " chars are allowed";
        }
        if (rulesType != null) {
            if (RulesType.NumericOnly.equals(rulesType)) {
                if (!StringUtils.isNumeric(text)) {
                    return "Numbers only allowed";
                }
            }
            if (RulesType.AlphaOnly.equals(rulesType)) {
                if (!StringUtils.isAlpha(text)) {
                    return "Chars only allowed";
                }
            }
            if (RulesType.Alphanumeric.equals(rulesType)) {
                if (!StringUtils.isAlphanumeric(text)) {
                    return "Chars and numbers allowed";
                }
            }
            if (RulesType.AlphanumericSpace.equals(rulesType)) {
                if (!StringUtils.isAlphanumericSpace(text)) {
                    return "Chars, numbers and space allowed";
                }
            }
        }
        return null;
    }

    public String getRulesMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Empty text not allowed \n");
        if (0 < minLength) {
            builder.append("Minimum ").append(minLength).append(" chars are required \n");
        }
        if (0 < maxLength) {
            builder.append("Maximum ").append(maxLength).append(" chars only allowed \n");
        }
        if (rulesType != null) {
            if (RulesType.NumericOnly.equals(rulesType)) {
                builder.append("Numbers only allowed \n");
            }
            if (RulesType.AlphaOnly.equals(rulesType)) {
                builder.append("Chars only allowed \n");
            }
            if (RulesType.Alphanumeric.equals(rulesType)) {
                builder.append("Chars and numbers are allowed \n");
            }
            if (RulesType.AlphanumericSpace.equals(rulesType)) {
                builder.append("Chars, numbers and space are allowed \n");
            }
        }
        return builder.toString();
    }
}
