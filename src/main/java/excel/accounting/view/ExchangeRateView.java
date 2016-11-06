package excel.accounting.view;

import excel.accounting.entity.ExchangeRate;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.ExchangeRateService;
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
import java.util.ArrayList;
import java.util.Date;
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
    private final String confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

    private ReadableTableView<ExchangeRate> tableView;
    private ExchangeRateService exchangeRateService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Management, "exchangeRateView", "Exchange Rate");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        exchangeRateService = (ExchangeRateService) getService("exchangeRateService");
        tableView = new ReadableTableView<ExchangeRate>().create();
        tableView.addIntegerColumn("id", "Id").setPrefWidth(60);
        tableView.addTextColumn("fetchFrom", "Reference From").setPrefWidth(200);
        tableView.addDateColumn("asOfDate", "Date").setPrefWidth(100);
        tableView.addTextColumn("currency", "Currency").setMinWidth(80);
        tableView.addTextColumn("exchangeCurrency", "Exchange Currency").setMinWidth(80);
        tableView.addIntegerColumn("unit", "Rate Unit").setMinWidth(80);
        tableView.addDecimalColumn("sellingRate", "Selling Rate").setMinWidth(100);
        tableView.addDecimalColumn("buyingRate", "Buying Rate").setMinWidth(100);
        tableView.addTextColumn("status", "Status").setMinWidth(100);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Update As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Update As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Update As Closed");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Exchange Rate");
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

    private void statusChangedEvent(String actionId) {
        if (!confirmDialog("Are you really wish to change status?")) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            exchangeRateService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            exchangeRateService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            exchangeRateService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        exchangeRateService.deleteExchangeRate(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<ExchangeRate> exchangeRateList = exchangeRateService.loadAll();
        if (exchangeRateList == null || exchangeRateList.isEmpty()) {
            return;
        }
        ObservableList<ExchangeRate> observableList = FXCollections.observableArrayList(exchangeRateList);
        tableView.setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<ExchangeRate> readExcelData = new ReadExcelData<>("", file, exchangeRateService);
        List<ExchangeRate> dataList = readExcelData.readRowData(exchangeRateService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            return;
        }
        List<Integer> existingIdList = exchangeRateService.findIdList();
        List<ExchangeRate> updateList = new ArrayList<>();
        List<ExchangeRate> insertList = new ArrayList<>();
        for (ExchangeRate exchangeRate : dataList) {
            if (existingIdList.contains(exchangeRate.getId())) {
                updateList.add(exchangeRate);
            } else {
                insertList.add(exchangeRate);
            }
        }
        if (!updateList.isEmpty()) {
            exchangeRateService.updateExchangeRate(updateList);
        }
        if (!insertList.isEmpty()) {
            exchangeRateService.insertExchangeRate(insertList);
        }
        loadRecords();
    }

    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "exchange-rate" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<ExchangeRate> writeExcelData = new WriteExcelData<>(actionId, file, exchangeRateService);
        if (exportSelectedActionId.equals(actionId)) {
            List<ExchangeRate> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(exchangeRateService.loadAll());
        }
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId,
                closedActionId);
    }

    private void performActionEvent(final String actionId) {
        switch (actionId) {
            case deleteActionId:
                deleteEvent();
                break;
            case exportActionId:
            case exportSelectedActionId:
                exportToExcelEvent(actionId);
                break;
            case confirmedActionId:
            case draftedActionId:
            case closedActionId:
                statusChangedEvent(actionId);
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
