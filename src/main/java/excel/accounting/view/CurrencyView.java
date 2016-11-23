package excel.accounting.view;

import excel.accounting.dao.CurrencyDao;
import excel.accounting.db.*;
import excel.accounting.entity.Currency;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.CurrencyService;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.FileHelper;
import excel.accounting.ui.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Currency View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class CurrencyView extends AbstractView implements ViewHolder, EntityDao<Currency>, SqlWriter,
        ListChangeListener<Integer>, ActionHandler {
    private final int IMPORT = 100, EXPORT = 110, EXPORT_SELECTED = 120;
    private final int DELETE_RECORD = 10, SET_DRAFTED = 20, SET_CONFIRMED = 30, SET_CLOSED = 40, REOPEN_CLOSED = 50;
    private final int LOAD_RECORD = 1000, REFRESH_RECORD = 1100;

    private ReadableTableView<Currency> tableView;
    private CurrencyDao currencyDao;
    private CurrencyService currencyService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Registers, "currencyView", "Currency");
    }

    @Override
    public Node createControl() {
        currencyDao = (CurrencyDao) getService("currencyDao");
        currencyService = (CurrencyService) getService("currencyService");
        tableView = new ReadableTableView<Currency>().create();
        tableView.addTextColumn("code", "Currency").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(260);
        tableView.addTextColumn("symbol", "Symbol").setPrefWidth(100);
        tableView.addTextColumn("decimalPrecision", "Precision").setMinWidth(120);
        tableView.addTextColumn("status", "Status").setMinWidth(120);
        tableView.addSelectionChangeListener(this);
        tableView.setContextMenuHandler(this);
        tableView.addContextMenuItem(SET_CONFIRMED, "Set As Confirmed");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(SET_DRAFTED, "Set As Drafted");
        tableView.addContextMenuItem(SET_CLOSED, "Set As Closed");
        tableView.addContextMenuItem(REOPEN_CLOSED, "Reopen Currency");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(EXPORT_SELECTED, "Export As xls");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(DELETE_RECORD, "Delete Currency");
        //
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), tableView.getTableView());
        return basePanel;
    }

    private HBox createToolbar() {
        Button refreshBtn, importBtn, exportBtn;
        refreshBtn = createButton(REFRESH_RECORD, "Refresh", event ->
                currencyDao.loadAll(LOAD_RECORD, this)
        );
        importBtn = createButton(IMPORT, "Import", event -> importFromExcelEvent());
        exportBtn = createButton(EXPORT, "Export", event -> currencyDao.loadAll(EXPORT, this));
        //
        HBox box = new HBox();
        box.setSpacing(12);
        box.setPadding(new Insets(12));
        box.getChildren().addAll(refreshBtn, importBtn, exportBtn);
        return box;
    }

    @Override
    public boolean canCloseView() {
        return true;
    }

    @Override
    public void closeView() {
    }

    @Override
    public void openView(double width, double height) {
        onResize(width, height);
        onRowSelectionChanged(false);
        currencyDao.loadAll(LOAD_RECORD, this);
    }

    @Override
    public void onWidthChanged(double width) {
        basePanel.setPrefWidth(width);
    }

    @Override
    public void onHeightChanged(double height) {
        basePanel.setPrefHeight(height);
    }

    private void changeStatusEvent(final int actionId) {
        String message = "";
        switch (actionId) {
            case SET_CONFIRMED:
                message = "Are you really wish to confirmed?";
                break;
            case SET_DRAFTED:
                message = "Are you really wish to drafted?";
                break;
            case SET_CLOSED:
                message = "Are you really wish to closed?";
                break;
            case REOPEN_CLOSED:
                message = "Are you really wish to reopen?";
                break;
        }
        if (!confirmDialog(message)) {
            return;
        }
        switch (actionId) {
            case SET_CONFIRMED:
                currencyService.setAsConfirmed(tableView.getSelectedItems(), actionId, this);
                break;
            case SET_DRAFTED:
                currencyService.setAsDrafted(tableView.getSelectedItems(), actionId, this);
                break;
            case SET_CLOSED:
                currencyService.setAsClosed(tableView.getSelectedItems(), actionId, this);
                break;
            case REOPEN_CLOSED:
                currencyService.reopenCurrency(tableView.getSelectedItems(), actionId, this);
                break;
        }
    }

    private void deleteEvent() {
        if (!confirmDialog("Are you really wish to delete?")) {
            return;
        }
        currencyService.deleteCurrency(tableView.getSelectedItems(), DELETE_RECORD, this);
    }

    @Override
    public void onEntityResult(int pid, List<Currency> dataList) {
        switch (pid) {
            case LOAD_RECORD:
                setItems(dataList);
                break;
            case EXPORT:
                exportToExcelEvent(dataList);
                break;
        }
    }

    @Override
    public void onSqlUpdated(int pid) {
        currencyDao.loadAll(LOAD_RECORD, this);
    }

    @Override
    public void onSqlError(int pid, SQLException ex) {
        ex.printStackTrace();
    }

    private void setItems(List<Currency> dataList) {
        ObservableList<Currency> observableList = FXCollections.observableArrayList(dataList);
        tableView.setItems(observableList);
    }

    @Override
    public void onEntityError(int pid, Exception ex) {
        ex.printStackTrace();
    }

    private void importFromExcelEvent() {
        setMessage("");
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Currency> readExcelData = new ReadExcelData<>("", file, currencyService);
        List<Currency> dataList = readExcelData.readRowData(currencyService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            setMessage("Valid import records not found");
            return;
        }
        currencyService.insertCurrency(dataList, 212, this);
    }

    private void exportToExcelEvent(List<Currency> currencyList) {
        String fileName = DataConverter.getUniqueFileName("currency", "xls");
        WriteExcelData<Currency> writeExcelData = new WriteExcelData<>(currencyList, currencyService);
        writeExcelData.writeRecords(fileName, getPrimaryStage());
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.disableAction(!isRowSelected, EXPORT_SELECTED, SET_DRAFTED, SET_CONFIRMED, SET_CLOSED);
    }

    private void onActionEvent(final int actionId) {
        setMessage("");
        switch (actionId) {
            case SET_CONFIRMED:
            case SET_DRAFTED:
            case SET_CLOSED:
            case REOPEN_CLOSED:
                changeStatusEvent(actionId);
                break;
            case REFRESH_RECORD:
                currencyDao.loadAll(LOAD_RECORD, this);
                break;
            case DELETE_RECORD:
                deleteEvent();
                break;
            case EXPORT:
                currencyDao.loadAll(EXPORT, this);
                break;
            case EXPORT_SELECTED:
                exportToExcelEvent(tableView.getSelectedItems());
                break;
            case IMPORT:
                importFromExcelEvent();
                break;
        }
    }

    @Override
    public void onChanged(ListChangeListener.Change<? extends Integer> change) {
        onRowSelectionChanged(0 < change.getList().size());
    }

    @Override
    public void onActionEvent(final String actionId) {
        onActionEvent(DataConverter.getInteger(actionId));
    }
}
