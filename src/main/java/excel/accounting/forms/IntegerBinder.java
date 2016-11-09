package excel.accounting.forms;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Integer Binder
 */
class IntegerBinder implements ValueBinder<Integer> {
    private final String name;
    private TextField field;
    private Label label;
    private int existingValue;

    IntegerBinder(String name, String title) {
        this.name = name;
        field = new TextField();
        label = new Label(title);
        label.setLabelFor(field);
        existingValue = 0;
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public Integer getFieldValue() {
        return getInteger(field.getText().trim());
    }

    @Override
    public void setFieldValue(Integer value) {
        if (value == null) {
            value = 0;
        }
        existingValue = value;
        field.setText(getString(value));
    }

    public void setText(String text) {
        int value = getInteger(text);
        field.setText(getString(value));
    }

    private int getInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NullPointerException ex) {
            // Ignore Exception
        }
        return 0;
    }

    private String getString(int value) {
        return value == 0 ? "" : value + "";
    }

    @Override
    public boolean isModified() {
        return existingValue != getFieldValue();
    }

    Label getLabel() {
        return label;
    }

    TextField getField() {
        return field;
    }
}
