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
    private boolean firstCharAlphaOnly;

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

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isAlpha(String text) {
        char[] chars = text.toCharArray();
        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFirstCharAlpha(String text) {
        char firstChar = text.charAt(0);
        return Character.isLetter(firstChar);
    }

    public void setFirstCharAlphaOnly(boolean firstCharAlphaOnly) {
        this.firstCharAlphaOnly = firstCharAlphaOnly;
    }

    public void setRulesType(RulesType rulesType) {
        this.rulesType = rulesType;
    }

    public boolean isValid(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        if (0 < minLength && minLength > text.length()) {
            return false;
        }
        if (0 < maxLength && maxLength < text.length()) {
            return false;
        }
        if (firstCharAlphaOnly && !isFirstCharAlpha(text)) {
            return false;
        }
        if (rulesType != null) {
            if (RulesType.NumericOnly.equals(rulesType) && !StringUtils.isNumeric(text)) {
                return false;
            }
            if (RulesType.AlphaOnly.equals(rulesType) && !isAlpha(text)) {
                return false;
            }
            if (RulesType.Alphanumeric.equals(rulesType) && !StringUtils.isAlphanumeric(text)) {
                return false;
            }
            if (RulesType.AlphanumericSpace.equals(rulesType) && !StringUtils.isAlphanumericSpace(text)) {
                return false;
            }
        }
        return true;
    }

    public String getMessage(String textValue) {
        if (textValue == null || textValue.trim().isEmpty()) {
            return "Empty text are not allowed";
        }
        final String text = textValue.trim();
        if (0 < minLength && minLength > text.length()) {
            return "Minimum " + minLength + " chars are required";
        }
        if (0 < maxLength && maxLength < text.length()) {
            return "Maximum " + maxLength + " chars are allowed";
        }
        if (firstCharAlphaOnly && !isFirstCharAlpha(text)) {
            return "First char letter only allowed";
        }
        if (rulesType != null) {
            if (RulesType.NumericOnly.equals(rulesType) && !StringUtils.isNumeric(text)) {
                return "Numbers only allowed";
            }
            if (RulesType.AlphaOnly.equals(rulesType) && !isAlpha(text)) {
                return "Chars only allowed";
            }
            if (RulesType.Alphanumeric.equals(rulesType) && !StringUtils.isAlphanumeric(text)) {
                return "Chars and numbers allowed";
            }
            if (RulesType.AlphanumericSpace.equals(rulesType) && !StringUtils.isAlphanumericSpace(text)) {
                return "Chars, numbers and space allowed";
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
