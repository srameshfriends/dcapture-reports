package excel.accounting.view;

import excel.accounting.dao.ExpenseItemDao;
import excel.accounting.dialog.AccountDialog;
import excel.accounting.dialog.CurrencyDialog;
import excel.accounting.dialog.ExpenseCategoryDialog;
import excel.accounting.dialog.ExpensePayableDialog;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.ExpenseItem;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.ExpenseItemService;
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
 * Expense Item View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExpenseItemView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String draftedActionId = "draftedAction", confirmedActionId = "confirmedAction";
    private final String updateCurrencyActionId = "updateCurrencyAction";
    private final String updateAccountActionId = "updateAccountAction", updatePaymentActionId = "updatePaymentAction";
    private final String updateCategoryActionId = "updateCategoryAction";

    private ReadableTableView<ExpenseItem> tableView;
    private ExpenseItemDao expenseItemDao;
    private ExpenseItemService expenseItemService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Expense, "expenseItemView", "Expense Entries");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        expenseItemDao = (ExpenseItemDao) getService("expenseItemDao");
        expenseItemService = (ExpenseItemService) getService("expenseItemService");
        tableView = new ReadableTableView<ExpenseItem>().create();
        tableView.addTextColumn("code", "Item Code").setPrefWidth(90);
        tableView.addTextColumn("groupCode", "Group Code").setPrefWidth(90);
        tableView.addTextColumn("expenseDate", "Expense Date").setPrefWidth(100);
        tableView.addTextColumn("referenceNumber", "Reference Num").setPrefWidth(160);
        tableView.addTextColumn("description", "Description").setPrefWidth(380);
        tableView.addTextColumn("expenseCategory", "Category").setMinWidth(120);
        tableView.addTextColumn("currency", "Currency").setMinWidth(80);
        tableView.addTextColumn("amount", "Amount").setMinWidth(140);
        tableView.addTextColumn("account", "Account").setMinWidth(120);
        tableView.addTextColumn("status", "Status").setMinWidth(100);
        tableView.addEnumColumn("paidStatus", "Paid").setMinWidth(60);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);

        tableView.addContextMenuItem(updateCategoryActionId, "Update Category");
        tableView.addContextMenuItem(updateCurrencyActionId, "Update Currency");
        tableView.addContextMenuItem(updateAccountActionId, "Update Account");
        tableView.addContextMenuItem(confirmedActionId, "Set As Confirmed");
        tableView.addContextMenuItem(updatePaymentActionId, "Add Payment");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(draftedActionId, "Set As Drafted");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(exportSelectedActionId, "Export as xls");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(deleteActionId, "Delete Expense items");
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
            expenseItemService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            expenseItemService.setAsDrafted(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        expenseItemService.deleteExpenseItem(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<ExpenseItem> categoryList = expenseItemDao.loadAll(ExpenseItem.class);
        if (categoryList == null || categoryList.isEmpty()) {
            return;
        }
        ObservableList<ExpenseItem> observableList = FXCollections.observableArrayList(categoryList);
        tableView.setItems(observableList);
    }

    private void importFromExcel() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<ExpenseItem> readExcelData = new ReadExcelData<>("", file, expenseItemService);
        List<ExpenseItem> dataList = readExcelData.readRowData(expenseItemService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            showErrorMessage("Valid expense items not found");
        } else if (expenseItemService.insertExpenseItem(dataList)) {
            loadRecords();
        }
    }

    private void exportToExcel(final String actionId) {
        String fileName = DataConverter.getUniqueFileName("expense-item", "xls");
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<ExpenseItem> writeExcelData = new WriteExcelData<>(actionId, file, expenseItemService);
        if (exportSelectedActionId.equals(actionId)) {
            List<ExpenseItem> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(expenseItemDao.loadAll(ExpenseItem.class));
        }
    }

    private void updateCurrency() {
        List<ExpenseItem> expenseItemList = tableView.getSelectedItems();
        if (expenseItemList.isEmpty()) {
            return;
        }
        CurrencyDialog dialog = new CurrencyDialog();
        dialog.setApplicationControl(getApplicationControl());
        dialog.start(getPrimaryStage());
        dialog.showAndWait();
        if (dialog.isCancelled()) {
            return;
        }
        expenseItemService.updateCurrency(dialog.getSelected(), expenseItemList);
        loadRecords();
    }

    private void updateExpenseAccount() {
        List<ExpenseItem> expenseItemList = tableView.getSelectedItems();
        if (expenseItemList.isEmpty()) {
            return;
        }
        AccountDialog dialog = new AccountDialog(AccountType.Expense);
        dialog.setApplicationControl(getApplicationControl());
        dialog.start(getPrimaryStage());
        dialog.showAndWait();
        if (dialog.isCancelled()) {
            return;
        }
        expenseItemService.updateExpenseAccount(dialog.getSelected(), expenseItemList);
        loadRecords();
    }

    private void updateExpenseCategory() {
        List<ExpenseItem> expenseItemList = tableView.getSelectedItems();
        if (expenseItemList.isEmpty()) {
            return;
        }
        ExpenseCategoryDialog dialog = new ExpenseCategoryDialog();
        dialog.setApplicationControl(getApplicationControl());
        dialog.start(getPrimaryStage());
        dialog.showAndWait();
        if (dialog.isCancelled()) {
            return;
        }
        expenseItemService.updateExpenseCategory(dialog.getSelected(), expenseItemList);
        loadRecords();
    }

    private void addPayments() {
        List<ExpenseItem> expenseItemList = tableView.getSelectedItems();
        if (expenseItemList.isEmpty()) {
            return;
        }
        ExpensePayableDialog dialog = new ExpensePayableDialog();
        dialog.setApplicationControl(getApplicationControl());
        dialog.start(getPrimaryStage());
        dialog.showAndWait();
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId);
    }

    private void performActionEvent(final String actionId) {
        switch (actionId) {
            case deleteActionId:
                deleteEvent();
                break;
            case exportActionId:
            case exportSelectedActionId:
                exportToExcel(actionId);
                break;
            case confirmedActionId:
            case draftedActionId:
                updateStatus(actionId);
                break;
            case updateCurrencyActionId:
                updateCurrency();
                break;
            case updateAccountActionId:
                updateExpenseAccount();
                break;
            case updatePaymentActionId:
                addPayments();
                break;
            case updateCategoryActionId:
                updateExpenseCategory();
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
