package excel.accounting.view;

import excel.accounting.dao.ExpenseCategoryDao;
import excel.accounting.entity.ExpenseCategory;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.ExpenseCategoryService;
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
 * Expense Category View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExpenseCategoryView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction", reopenActionId = "reopenAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";
    private final String updateChartOfAccActionId = "updateChartOfAccAction";

    private ReadableTableView<ExpenseCategory> tableView;
    private ExpenseCategoryDao expenseCategoryDao;
    private ExpenseCategoryService expenseCategoryService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Expense, "expenseCategoryView", "Expense Category");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        expenseCategoryService = (ExpenseCategoryService) getService("expenseCategoryService");
        expenseCategoryDao = (ExpenseCategoryDao) getService("expenseCategoryDao");
        tableView = new ReadableTableView<ExpenseCategory>().create();
        tableView.addTextColumn("code", "Category Code").setPrefWidth(100);
        tableView.addTextColumn("name", "Name").setPrefWidth(160);
        tableView.addTextColumn("chartOfAccounts", "Chart Of Accounts").setPrefWidth(100);
        tableView.addTextColumn("description", "Description").setMinWidth(200);
        tableView.addTextColumn("status", "Status").setMinWidth(80);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(confirmedActionId, "Update As Confirmed");
        tableView.addContextMenuItem(draftedActionId, "Update As Drafted");
        tableView.addContextMenuItem(closedActionId, "Update As Closed");
        tableView.addContextMenuItem(reopenActionId, "Reopen Expense Category");
        tableView.addContextMenuItem(updateChartOfAccActionId, "Set Chart Of Accounts");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Expense Category");
        tableView.addContextMenuItem(deleteActionId, "Delete Expense Category");
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
        if (!confirmDialog("Update Expense Category Status",
                "Are you really wish to change selected income category Status?")) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            expenseCategoryService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            expenseCategoryService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            expenseCategoryService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteExpenseCategory() {
        expenseCategoryService.deleteExpenseCategory(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<ExpenseCategory> categoryList = expenseCategoryDao.loadAll();
        if (categoryList == null || categoryList.isEmpty()) {
            return;
        }
        ObservableList<ExpenseCategory> observableList = FXCollections.observableArrayList(categoryList);
        tableView.setItems(observableList);
    }

    private void updateChartOfAccounts() {
       //
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<ExpenseCategory> readExcelData = new ReadExcelData<>("", file, expenseCategoryService);
        List<ExpenseCategory> dataList = readExcelData.readRowData(expenseCategoryService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            return;
        }
        List<String> existingList = expenseCategoryDao.findCodeList();
        List<ExpenseCategory> updateList = new ArrayList<>();
        List<ExpenseCategory> insertList = new ArrayList<>();
        for (ExpenseCategory category : dataList) {
            if (existingList.contains(category.getCode())) {
                updateList.add(category);
            } else {
                insertList.add(category);
            }
        }
        if (!updateList.isEmpty()) {
            expenseCategoryService.updateExpenseCategory(updateList);
        }
        if (!insertList.isEmpty()) {
            expenseCategoryService.insertExpenseCategory(insertList);
        }
        loadRecords();
    }

    /*
    id, code, name, status, currency, debit_account, credit_account, description
    */
    private void exportToExcel(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "expense-category" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<ExpenseCategory> writeExcelData = new WriteExcelData<>(actionId, file, expenseCategoryService);
        if (exportSelectedActionId.equals(actionId)) {
            List<ExpenseCategory> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(expenseCategoryDao.loadAll());
        }

    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId,
                closedActionId);
    }

    private void performActionEvent(final String actionId) {
        switch (actionId) {
            case deleteActionId:
                deleteExpenseCategory();
                break;
            case exportActionId:
            case exportSelectedActionId:
                exportToExcel(actionId);
                break;
            case confirmedActionId:
            case draftedActionId:
            case closedActionId:
            case reopenActionId:
                updateStatus(actionId);
                break;
            case updateChartOfAccActionId:
                updateChartOfAccounts();
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
