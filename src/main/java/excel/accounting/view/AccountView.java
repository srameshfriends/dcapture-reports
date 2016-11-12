package excel.accounting.view;

import excel.accounting.dao.AccountDao;
import excel.accounting.dialog.CurrencyDialog;
import excel.accounting.dialog.EnumSelectionDialog;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.shared.DataConverter;
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
    private final String closedActionId = "closedAction", reopenActionId = "reopenAction";
    private final String updateCurrencyActionId = "updateCurrencyAction";
    private final String updateAccountTypeActionId = "updateAccountTypeAction";

    private ReadableTableView<Account> tableView;
    private AccountDao accountDao;
    private AccountService accountService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Registers, "accountView", "Account");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        accountDao = (AccountDao) getService("accountDao");
        accountService = (AccountService) getService("accountService");
        tableView = new ReadableTableView<Account>().create();
        tableView.addTextColumn("code", "Account Number").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(220);
        tableView.addTextColumn("description", "Description").setPrefWidth(260);
        tableView.addEnumColumn("accountType", "Account Type").setMinWidth(120);
        tableView.addTextColumn("currency", "Currency").setMinWidth(60);
        tableView.addDecimalColumn("balance", "Account Balance").setMinWidth(160);
        tableView.addEnumColumn("status", "Status").setMinWidth(80);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(updateCurrencyActionId, "Set Currency");
        tableView.addContextMenuItem(updateAccountTypeActionId, "Set Account Type");
        tableView.addContextMenuItem(confirmedActionId, "Set As Confirmed");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(draftedActionId, "Set As Drafted");
        tableView.addContextMenuItem(closedActionId, "Set As Closed");
        tableView.addContextMenuItem(reopenActionId, "Reopen Account");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(exportSelectedActionId, "Export As xls");
        tableView.addContextMenuItemSeparator();
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
        String message = "Error : Unknown action id " + actionId;
        if (confirmedActionId.equals(actionId)) {
            message = "Are you really wish to Confirmed?";
        } else if (draftedActionId.equals(actionId)) {
            message = "Are you really wish to Drafted?";
        } else if (closedActionId.equals(actionId)) {
            message = "Are you really wish to Closed?";
        } else if (reopenActionId.equals(actionId)) {
            message = "Are you really wish to Reopen?";
        }
        if (!confirmDialog(message)) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            accountService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            accountService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            accountService.setAsClosed(tableView.getSelectedItems());
        } else if (reopenActionId.equals(actionId)) {
            accountService.reopenAccount(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        if (!confirmDialog("Are you really wish to delete?")) {
            return;
        }
        accountService.deleteAccount(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<Account> accountList = accountDao.loadAll();
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        tableView.setItems(observableList);
    }

    private void updateCurrency() {
        CurrencyDialog dialog = new CurrencyDialog();
        dialog.setApplicationControl(getApplicationControl());
        dialog.start(getPrimaryStage());
        dialog.showAndWait();
        if (dialog.isCancelled()) {
            return;
        }
        List<Account> accountList = tableView.getSelectedItems();
        if (accountList != null) {
            accountService.updateCurrency(dialog.getSelected(), accountList);
            loadRecords();
        }
    }

    private void updateAccountType() {
        EnumSelectionDialog<AccountType> dialog = new EnumSelectionDialog<>();
        dialog.setApplicationControl(getApplicationControl());
        dialog.start(getPrimaryStage());
        dialog.setValueList(AccountType.values());
        dialog.showAndWait();
        if (dialog.isCancelled()) {
            return;
        }
        List<Account> accountList = tableView.getSelectedItems();
        if (accountList != null) {
            accountService.updateAccountType(dialog.getSelected(), accountList);
            loadRecords();
        }
    }

    private void importFromExcel() {
        setMessage("");
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Account> readExcelData = new ReadExcelData<>("", file, accountService);
        List<Account> dataList = readExcelData.readRowData(accountService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            setMessage("Valid import records not found");
            return;
        }
        accountService.insertAccount(dataList);
        loadRecords();
    }

    private void exportToExcel(final String actionId) {
        String fileName = DataConverter.getUniqueFileName("accounts", "xls");
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<Account> writeExcelData = new WriteExcelData<>(actionId, file, accountService);
        if (exportSelectedActionId.equals(actionId)) {
            List<Account> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(accountDao.loadAll());
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
                exportToExcel(actionId);
                break;
            case confirmedActionId:
            case draftedActionId:
            case closedActionId:
            case reopenActionId:
                updateStatus(actionId);
                break;
            case updateCurrencyActionId:
                updateCurrency();
                break;
            case updateAccountTypeActionId:
                updateAccountType();
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
            setMessage("");
            performActionEvent(actionId);
        }
    }
}
