package excel.accounting.forms;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * BigDecimal Binder
 */
public class BigDecimalBinder implements ValueBinder<BigDecimal> {
    private final String name;
    private TextField field;
    private Label label;
    private BigDecimal existingValue;
    private DecimalFormat decimalFormat;

    BigDecimalBinder(String name, String title) {
        this.name = name;
        field = new TextField();
        label = new Label(title);
        label.setLabelFor(field);
        existingValue = BigDecimal.ZERO;
    }

    public void setDecimalFormat(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public BigDecimal getFieldValue() {
        return getDecimal(field.getText().trim());
    }

    @Override
    public void setFieldValue(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        existingValue = value;
        field.setText(getString(value));
    }

    public void setText(String text) {
        BigDecimal value = getDecimal(text);
        field.setText(getString(value));
    }

    private BigDecimal getDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NullPointerException ex) {
            // Ignore Exception
        }
        return BigDecimal.ZERO;
    }

    private String getString(BigDecimal value) {
        if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
            return "";
        }
        if (decimalFormat != null) {
            return decimalFormat.format(value);
        }
        return value.toPlainString();
    }

    @Override
    public boolean isModified() {
        return !existingValue.equals(getFieldValue());
    }

    public Label getLabel() {
        return label;
    }

    public TextField getField() {
        return field;
    }
}
