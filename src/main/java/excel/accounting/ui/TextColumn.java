package excel.accounting.ui;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Editable Text Column
 *
 * @author Ramesh
 * @since Nov, 2016
 */
class TextColumn<T> implements Callback<TableColumn<T, String>, TableCell<T, String>>,
        EventHandler<TableColumn.CellEditEvent<T, String>> {
    private final TableColumnHandler handler;
    private final String fieldName;

    TextColumn(String fieldName, TableColumnHandler modifiedHandler) {
        this.fieldName = fieldName;
        this.handler = modifiedHandler;
    }

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {
        return new EditableTextCell<T>();
    }

    @Override
    public void handle(TableColumn.CellEditEvent<T, String> event) {
        int index = event.getTablePosition().getRow();
        handler.onEditableRowEvent(index, fieldName, event.getOldValue(), event.getNewValue());
    }
}
