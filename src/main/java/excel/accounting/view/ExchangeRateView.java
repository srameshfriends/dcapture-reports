package excel.accounting.view;

import excel.accounting.dao.ExchangeRateDao;
import excel.accounting.entity.ExchangeRate;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.ExchangeRateService;
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
import java.util.List;

/**
 * Exchange Rate View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExchangeRateView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction", draftedActionId = "draftedAction";

    private ReadableTableView<ExchangeRate> tableView;
    private ExchangeRateDao exchangeRateDao;
    private ExchangeRateService exchangeRateService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Management, "exchangeRateView", "Exchange Rate");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        exchangeRateDao = (ExchangeRateDao) getService("exchangeRateDao");
        exchangeRateService = (ExchangeRateService) getService("exchangeRateService");
        tableView = new ReadableTableView<ExchangeRate>().create();
        tableView.addTextColumn("code", "Code").setPrefWidth(60);
        tableView.addTextColumn("fetchFrom", "Reference From").setPrefWidth(200);
        tableView.addDateColumn("asOfDate", "Date").setPrefWidth(100);
        tableView.addTextColumn("currency", "Currency").setMinWidth(80);
        tableView.addTextColumn("exchangeCurrency", "Exchange Currency").setMinWidth(80);
        tableView.addIntegerColumn("unit", "Rate Unit").setMinWidth(80);
        tableView.addDecimalColumn("sellingRate", "Selling Rate").setMinWidth(100);
        tableView.addDecimalColumn("buyingRate", "Buying Rate").setMinWidth(100);
        tableView.addEnumColumn("status", "Status").setMinWidth(100);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(confirmedActionId, "Set As Confirmed");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(draftedActionId, "Set As Drafted");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(exportSelectedActionId, "Export Exchange Rate");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(deleteActionId, "Delete Exchange Rate");
        //
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), tableView.getTableView());
        return basePanel;
    }

    private HBox createToolbar() {
        final String importActionId = "importAction", refreshActionId = "refreshAction";
        Button refreshBtn, importBtn, exportBtn;
        refreshBtn = createButton(refreshActionId, "Refresh", event -> loadRecords());
        importBtn = createButton(importActionId, "Import", event -> importFromExcel());
        exportBtn = createButton(exportActionId, "Export", event -> exportToExcel(exportActionId));
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

    private void updateStatus(String actionId) {
        String message = "";
        if (confirmedActionId.equals(actionId)) {
            message = "Are you really wish to Confirmed?";
        } else if (draftedActionId.equals(actionId)) {
            message = "Are you really wish to Drafted?";
        }
        if (!confirmDialog(message)) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            exchangeRateService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            exchangeRateService.setAsDrafted(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteExchangeRate() {
        exchangeRateService.deleteExchangeRate(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<ExchangeRate> exchangeRateList = exchangeRateDao.loadAll();
        if (exchangeRateList == null || exchangeRateList.isEmpty()) {
            return;
        }
        ObservableList<ExchangeRate> observableList = FXCollections.observableArrayList(exchangeRateList);
        tableView.setItems(observableList);
    }

    private void importFromExcel() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<ExchangeRate> readExcelData = new ReadExcelData<>("", file, exchangeRateService);
        List<ExchangeRate> dataList = readExcelData.readRowData(exchangeRateService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            showErrorMessage("Valid exchange rate not found");
            return;
        }
        if (exchangeRateService.insertExchangeRate(dataList)) {
            loadRecords();
        }
    }

    private void exportToExcel(final String actionId) {
        String fileName = DataConverter.getUniqueFileName("exchange-rate", "xls");
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<ExchangeRate> writeExcelData = new WriteExcelData<>(actionId, file, exchangeRateService);
        if (exportSelectedActionId.equals(actionId)) {
            List<ExchangeRate> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(exchangeRateDao.loadAll());
        }
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId);
    }

    private void performActionEvent(final String actionId) {
        switch (actionId) {
            case deleteActionId:
                deleteExchangeRate();
                break;
            case exportActionId:
            case exportSelectedActionId:
                exportToExcel(actionId);
                break;
            case confirmedActionId:
            case draftedActionId:
                updateStatus(actionId);
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
