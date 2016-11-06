package excel.accounting.view;

import excel.accounting.entity.Asset;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.AssetService;
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
 * Asset View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class AssetView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

    private ReadableTableView<Asset> tableView;
    private AssetService assetService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Assets, "assetView", "Assets Entries");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        assetService = (AssetService) getService("assetService");
        tableView = new ReadableTableView<Asset>().create();
        tableView.addTextColumn("code", "Code").setPrefWidth(100);
        tableView.addTextColumn("name", "Name").setPrefWidth(160);
        tableView.addTextColumn("description", "Description").setPrefWidth(200);
        tableView.addTextColumn("assetType", "Asset Type").setMinWidth(100);
        tableView.addDateColumn("startDate", "Start Date").setMinWidth(80);
        tableView.addDateColumn("endDate", "End Date").setMinWidth(80);
        tableView.addTextColumn("currency", "Currency").setMinWidth(60);
        tableView.addDecimalColumn("cost", "Cost").setMinWidth(120);
        tableView.addDecimalColumn("units", "Units").setMinWidth(60);
        tableView.addTextColumn("referenceNumber", "Reference Number").setMinWidth(120);
        tableView.addTextColumn("category", "Category").setMinWidth(160);
        tableView.addTextColumn("status", "Status").setMinWidth(120);

        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Update As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Update As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Update As Closed");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Asset");
        tableView.addContextMenuItem(deleteActionId, "Delete Asset");
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
        if (!confirmDialog("Are you really wish to change selected assets status?")) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            assetService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            assetService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            assetService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        assetService.deleteAsset(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<Asset> assetList = assetService.loadAll();
        if (assetList == null || assetList.isEmpty()) {
            return;
        }
        ObservableList<Asset> observableList = FXCollections.observableArrayList(assetList);
        tableView.setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Asset> readExcelData = new ReadExcelData<>("", file, assetService);
        List<Asset> rowDataList = readExcelData.readRowData(assetService.getColumnNames().length, true);
        if (rowDataList.isEmpty()) {
            return;
        }
        List<String> existingCodeList = assetService.findCodeList();
        List<Asset> updateList = new ArrayList<>();
        List<Asset> insertList = new ArrayList<>();
        for (Asset asset : rowDataList) {
            if (existingCodeList.contains(asset.getCode())) {
                updateList.add(asset);
            } else {
                insertList.add(asset);
            }
        }
        if (!updateList.isEmpty()) {
            assetService.updateAsset(updateList);
        }
        if (!insertList.isEmpty()) {
            assetService.insertAsset(insertList);
        }
        loadRecords();
    }

    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "assets" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<Asset> writeExcelData = new WriteExcelData<>(actionId, file, assetService);
        if (exportSelectedActionId.equals(actionId)) {
            List<Asset> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(assetService.loadAll());
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
