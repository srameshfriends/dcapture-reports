package excel.accounting.view;

import excel.accounting.dao.IncomeItemDao;
import excel.accounting.entity.IncomeItem;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.IncomeItemService;
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
 * Income Item View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class IncomeItemView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

    private ReadableTableView<IncomeItem> tableView;
    private IncomeItemService incomeItemService;
    private IncomeItemDao incomeItemDao;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Income, "incomeItemView", "Income Entries");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        incomeItemService = (IncomeItemService) getService("incomeItemService");
        tableView = new ReadableTableView<IncomeItem>().create();
        tableView.addTextColumn("id", "Id").setPrefWidth(60);
        tableView.addTextColumn("incomeDate", "Income Date").setPrefWidth(100);
        tableView.addTextColumn("description", "Description").setPrefWidth(380);
        tableView.addTextColumn("currency", "Currency").setMinWidth(80);
        tableView.addTextColumn("amount", "Amount").setMinWidth(100);
        tableView.addTextColumn("status", "Status").setMinWidth(100);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Update As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Update As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Update As Closed");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Income Items");
        tableView.addContextMenuItem(deleteActionId, "Delete Income Items");
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
            incomeItemService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            incomeItemService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            incomeItemService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        incomeItemService.deleteIncomeItem(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<IncomeItem> categoryList = incomeItemDao.loadAll(IncomeItem.class);
        if (categoryList == null || categoryList.isEmpty()) {
            return;
        }
        ObservableList<IncomeItem> observableList = FXCollections.observableArrayList(categoryList);
        tableView.setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<IncomeItem> readExcelData = new ReadExcelData<>("", file, incomeItemService);
        List<IncomeItem> dataList = readExcelData.readRowData(incomeItemService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            return;
        }
        List<String> existingCodeList = incomeItemDao.loadCodeList();
        List<IncomeItem> updateList = new ArrayList<>();
        List<IncomeItem> insertList = new ArrayList<>();
        for (IncomeItem incomeItem : dataList) {
            if (existingCodeList.contains(incomeItem.getCode())) {
                updateList.add(incomeItem);
            } else {
                insertList.add(incomeItem);
            }
        }
        if (!updateList.isEmpty()) {
            incomeItemService.updateIncomeItem(updateList);
        }
        if (!insertList.isEmpty()) {
            incomeItemService.insertIncomeItem(insertList);
        }
        loadRecords();
    }

    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "income-item" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<IncomeItem> writeExcelData = new WriteExcelData<>(actionId, file, incomeItemService);
        if (exportSelectedActionId.equals(actionId)) {
            List<IncomeItem> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(incomeItemDao.loadAll(IncomeItem.class));
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
