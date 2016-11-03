package excel.accounting.view;

import excel.accounting.dao.CurrencyDao;
import excel.accounting.entity.Currency;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.CurrencyService;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Currency View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class CurrencyView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction", reopenActionId = "reopenAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

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
        ViewListener viewListener = new ViewListener();
        currencyDao = (CurrencyDao) getService("currencyDao");
        currencyService = (CurrencyService) getService("currencyService");
        tableView = new ReadableTableView<Currency>().create();
        tableView.addTextColumn("code", "Currency").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(260);
        tableView.addTextColumn("symbol", "Symbol").setPrefWidth(100);
        tableView.addTextColumn("decimalPrecision", "Precision").setMinWidth(120);
        tableView.addTextColumn("status", "Status").setMinWidth(120);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Set As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Set As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Set As Closed");
        tableView.addContextMenuItem(reopenActionId, "Reopen Currency");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Currency");
        tableView.addContextMenuItem(deleteActionId, "Delete Currency");
        //
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), tableView.getTableView());
        return basePanel;
    }

    private HBox createToolbar() {
        final String importActionId = "importAction", refreshActionId = "refreshAction";
        Button refreshBtn, importBtn, exportBtn;
        refreshBtn = createButton(refreshActionId, "Refresh", event -> loadRecords());
        importBtn = createButton(importActionId, "Import", event -> importFromExcelEvent());
        exportBtn = createButton(exportActionId, "Export", event -> exportToExcelEvent(exportActionId));
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
        loadRecords();
    }

    @Override
    public void onWidthChanged(double width) {
        basePanel.setPrefWidth(width);
    }

    @Override
    public void onHeightChanged(double height) {
        basePanel.setPrefHeight(height);
    }

    private void changeStatusEvent(String actionId) {
        String message = "";
        if (confirmedActionId.equals(actionId)) {
            message = "Are you really wish to change to confirmed?";
        } else if (draftedActionId.equals(actionId)) {
            message = "Are you really wish to change to drafted?";
        } else if (closedActionId.equals(actionId)) {
            message = "Are you really wish to change to closed?";
        } else if (reopenActionId.equals(actionId)) {
            message = "Are you really wish to reopen as confirmed status?";
        }
        if (!confirmDialog("Confirmation", message)) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            currencyService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            currencyService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            currencyService.setAsClosed(tableView.getSelectedItems());
        } else if (reopenActionId.equals(actionId)) {
            currencyService.reopenCurrency(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        if (!confirmDialog("Delete?", "Are you really wish to delete selected currencies?")) {
            return;
        }
        currencyService.deleteCurrency(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<Currency> currencyList = currencyDao.loadAll();
        if (currencyList == null || currencyList.isEmpty()) {
            return;
        }
        ObservableList<Currency> observableList = FXCollections.observableArrayList(currencyList);
        tableView.setItems(observableList);
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
        currencyService.insertCurrency(dataList);
        loadRecords();
    }

    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "currency" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<Currency> writeExcelData = new WriteExcelData<>(actionId, file, currencyService);
        if (exportSelectedActionId.equals(actionId)) {
            List<Currency> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(currencyDao.loadAll());
        }
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId,
                closedActionId);
    }

    private void performActionEvent(final String actionId) {
        setMessage("");
        switch (actionId) {
            case confirmedActionId:
            case draftedActionId:
            case closedActionId:
            case reopenActionId:
                changeStatusEvent(actionId);
                break;
            case deleteActionId:
                deleteEvent();
                break;
            case exportActionId:
            case exportSelectedActionId:
                exportToExcelEvent(actionId);
                break;
        }
    }

    private class ViewListener implements ListChangeListener<Integer>, ActionHandler {
        @Override
        public void onChanged(Change<? extends Integer> change) {
            onRowSelectionChanged(0 < change.getList().size());
        }

        @Override
        public void onActionEvent(final String actionId) {
            performActionEvent(actionId);
        }
    }
}
