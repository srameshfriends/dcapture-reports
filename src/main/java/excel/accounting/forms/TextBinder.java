package excel.accounting.forms;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Text Binder
 */
public class TextBinder implements ValueBinder<String> {
    private final String name;
    private TextField field;
    private Label label;
    private String existingValue;

    TextBinder(String name, String title) {
        this.name = name;
        field = new TextField();
        label = new Label(title);
        label.setLabelFor(field);
        existingValue = "";
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public String getFieldValue() {
        return field.getText().trim();
    }

    @Override
    public void setFieldValue(String value) {
        if (value == null) {
            value = "";
        }
        existingValue = value.trim();
        field.setText(value.trim());
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
