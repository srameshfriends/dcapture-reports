package excel.accounting.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Data List View
 */
public class ReadableTableView<T> {
    private TableView<T> tableView;
    private ContextMenu contextMenu;
    private ActionHandler contextHandler;

    public ReadableTableView<T> create() {
        tableView = new TableView<>();
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return ReadableTableView.this;
    }

    public void setContextMenuHandler(ActionHandler actionHandler) {
        this.contextHandler = actionHandler;
        contextMenu = new ContextMenu();
        tableView.setContextMenu(contextMenu);
    }

    public void addContextMenuItem(final String actionId, String title) {
        StyleBuilder builder = new StyleBuilder();
        builder.padding(2);
        MenuItem menuItem = new MenuItem(title);
        menuItem.setStyle(builder.toString());
        menuItem.setOnAction(event -> contextHandler.onActionEvent(actionId));
        contextMenu.getItems().add(menuItem);
    }

    public void addContextMenuItemSeparator() {
        StyleBuilder builder = new StyleBuilder();
        builder.padding(2);
        SeparatorMenuItem menuItem = new SeparatorMenuItem();
        menuItem.setStyle(builder.toString());
        contextMenu.getItems().add(menuItem);
    }

    public void setDisable(boolean disable, String... actionArray) {
        List<MenuItem> itemList = contextMenu.getItems();
        for (MenuItem menuItem : itemList) {
            for (String actionId : actionArray) {
                if (actionId.equals(menuItem.getId())) {
                    menuItem.setDisable(disable);
                    break;
                }
            }
        }
    }

    public void addSelectionChangeListener(ListChangeListener<Integer> listener) {
        tableView.getSelectionModel().getSelectedIndices().addListener(listener);
    }

    public TableColumn<T, String> addTextColumn(String name, String title) {
        TableColumn<T, String> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<T, BigDecimal> addDecimalColumn(String name, String title) {
        TableColumn<T, BigDecimal> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setStyle("-fx-alignment: center-right;");
        column.setCellValueFactory(new PropertyValueFactory<>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<T, Integer> addIntegerColumn(String name, String title) {
        TableColumn<T, Integer> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<T, Date> addDateColumn(String name, String title) {
        TableColumn<T, Date> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<T, Boolean> addBooleanColumn(String name, String title) {
        TableColumn<T, Boolean> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<T, Enum<?>> addEnumColumn(String name, String title) {
        TableColumn<T, Enum<?>> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        tableView.getSelectionModel().setSelectionMode(selectionMode);
    }

    public List<T> getSelectedItems() {
        return tableView.getSelectionModel().getSelectedItems();
    }

    public T getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    public TableView<T> getTableView() {
        return tableView;
    }

    public void setItems(ObservableList<T> list) {
        removeAll();
        tableView.getItems().addAll(list);
    }

    private void removeAll() {
        tableView.getItems().removeAll(tableView.getItems());
    }
}
