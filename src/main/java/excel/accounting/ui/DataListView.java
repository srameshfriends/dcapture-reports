package excel.accounting.ui;

import excel.accounting.model.RowData;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Data List View
 */
public class DataListView {
    private final TableView<RowData> tableView;

    public DataListView() {
        tableView = new TableView<>();
    }

    public TableColumn<RowData, String> addTextColumn(String name, String title) {
        TableColumn<RowData, String> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<RowData, String>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<RowData, BigDecimal> addDecimalColumn(String name, String title) {
        TableColumn<RowData, BigDecimal> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<RowData, BigDecimal>(name));
        tableView.getColumns().add(column);

        return column;
    }

    public TableColumn<RowData, Integer> addIntegerColumn(String name, String title) {
        TableColumn<RowData, Integer> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<RowData, Integer>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<RowData, Date> addDateColumn(String name, String title) {
        TableColumn<RowData, Date> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<RowData, Date>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<RowData, Boolean> addBooleanColumn(String name, String title) {
        TableColumn<RowData, Boolean> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<RowData, Boolean>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableView<RowData> getTableView() {
        return tableView;
    }
}
