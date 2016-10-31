package excel.accounting.view;

import excel.accounting.dialog.CurrencyDialog;
import excel.accounting.entity.Account;
import excel.accounting.ui.*;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.AccountService;
import excel.accounting.shared.FileHelper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Account View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class AccountView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String draftedActionId = "draftedAction", confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", updateCurrencyActionId = "updateCurrencyAction";

    private ReadableTableView<Account> tableView;
    private AccountService accountService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Registers, "accountView", "Account");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        accountService = (AccountService) getService("accountService");
        tableView = new ReadableTableView<Account>().create();
        tableView.addTextColumn("accountNumber", "Account Number").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(220);
        tableView.addTextColumn("description", "Description").setPrefWidth(260);
        tableView.addEnumColumn("accountType", "Account Type").setMinWidth(120);
        tableView.addTextColumn("currency", "Currency").setMinWidth(60);
        tableView.addDecimalColumn("balance", "Account Balance").setMinWidth(160);
        tableView.addTextColumn("status", "Status").setMinWidth(80);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Set As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Set As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Set As Closed");
        tableView.addContextMenuItem(updateCurrencyActionId, "Update Currency");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Accounts");
        tableView.addContextMenuItem(deleteActionId, "Delete Accounts");
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
        String message = "Error : Unknown action id " + actionId;
        if (confirmedActionId.equals(actionId)) {
            message = "Are you really wish to change status as Confirmed?";
        } else if (draftedActionId.equals(actionId)) {
            message = "Are you really wish to change status as Drafted?";
        } else if (closedActionId.equals(actionId)) {
            message = "Are you really wish to change status as Closed?";
        }
        if (!confirmDialog("Status Update", message)) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            accountService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            accountService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            accountService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        accountService.deleteAccount(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<Account> accountList = accountService.loadAll();
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        tableView.setItems(observableList);
    }

    private void updateCurrency() {
        CurrencyDialog dialog = new CurrencyDialog(getApplicationControl(), getPrimaryStage());
        dialog.showAndWait();
        if (dialog.isCancelled() || dialog.getSelected() == null) {
            return;
        }
        List<Account> accountList = tableView.getSelectedItems();
        if (accountList != null) {
            accountService.updateCurrency(dialog.getSelected(), accountList);
            loadRecords();
        }
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Account> readExcelData = new ReadExcelData<>("", file, accountService);
        List<Account> rowDataList = readExcelData.readRowData(accountService.getColumnNames().length, true);
        if (rowDataList.isEmpty()) {
            return;
        }
        List<String> existingNumberList = accountService.findAccountNumberList();
        List<Account> updateList = new ArrayList<>();
        List<Account> insertList = new ArrayList<>();
        for (Account account : rowDataList) {
            if (existingNumberList.contains(account.getAccountNumber())) {
                updateList.add(account);
            } else {
                insertList.add(account);
            }
        }
        if (!updateList.isEmpty()) {
            accountService.updateAccount(updateList);
        }
        if (!insertList.isEmpty()) {
            accountService.insertAccount(insertList);
        }
        loadRecords();
    }

    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "accounts" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<Account> writeExcelData = new WriteExcelData<>(actionId, file, accountService);
        if (exportSelectedActionId.equals(actionId)) {
            List<Account> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(accountService.loadAll());
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
            case updateCurrencyActionId:
                updateCurrency();
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
