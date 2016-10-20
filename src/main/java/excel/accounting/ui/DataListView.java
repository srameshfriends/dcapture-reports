package excel.accounting.ui;

import excel.accounting.model.EntityRow;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Data List View
 */
public class DataListView {
    private final TableView<EntityRow> tableView;

    public DataListView() {
        tableView = new TableView<>();
    }

    public TableColumn<EntityRow, String> addTextColumn(String name, String title) {
        TableColumn<EntityRow, String> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<EntityRow, String>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<EntityRow, BigDecimal> addDecimalColumn(String name, String title) {
        TableColumn<EntityRow, BigDecimal> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<EntityRow, BigDecimal>(name));
        tableView.getColumns().add(column);

        return column;
    }

    public TableColumn<EntityRow, Integer> addIntegerColumn(String name, String title) {
        TableColumn<EntityRow, Integer> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<EntityRow, Integer>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<EntityRow, Date> addDateColumn(String name, String title) {
        TableColumn<EntityRow, Date> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<EntityRow, Date>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableColumn<EntityRow, Boolean> addBooleanColumn(String name, String title) {
        TableColumn<EntityRow, Boolean> column = new TableColumn<>();
        column.setId(name);
        column.setText(title);
        column.setCellValueFactory(new PropertyValueFactory<EntityRow, Boolean>(name));
        tableView.getColumns().add(column);
        return column;
    }

    public TableView<EntityRow> getTableView() {
        return tableView;
    }
}
