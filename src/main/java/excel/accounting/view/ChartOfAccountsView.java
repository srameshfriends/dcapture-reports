package excel.accounting.view;

import excel.accounting.dao.ChartOfAccountsDao;
import excel.accounting.dialog.CurrencyDialog;
import excel.accounting.dialog.EnumSelectionDialog;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.ChartOfAccounts;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.ChartOfAccountsService;
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
 * Chart Of Accounts View
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class ChartOfAccountsView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String draftedActionId = "draftedAction", confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", reopenActionId = "reopenAction";
    private final String changeCurrencyActionId = "modifyCurrencyAction";
    private final String changeAccountTypeActionId = "changeAccountTypeAction";

    private ReadableTableView<ChartOfAccounts> tableView;
    private ChartOfAccountsDao chartOfAccountsDao;
    private ChartOfAccountsService chartOfAccService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Registers, "chartOfAccountsView", "Chart Of Accounts");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        chartOfAccountsDao = (ChartOfAccountsDao) getService("chartOfAccountsDao");
        chartOfAccService = (ChartOfAccountsService) getService("chartOfAccountsService");
        tableView = new ReadableTableView<ChartOfAccounts>().create();
        tableView.addTextColumn("code", "Account Number").setPrefWidth(120);
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
        tableView.addContextMenuItem(reopenActionId, "Reopen Account");
        tableView.addContextMenuItem(changeAccountTypeActionId, "Set Account Type");
        tableView.addContextMenuItem(changeCurrencyActionId, "Set Currency");

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
        } else if (reopenActionId.equals(actionId)) {
            message = "Are you really wish to reopen accounts?";
        }
        if (!confirmDialog("Status Update", message)) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            chartOfAccService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            chartOfAccService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            chartOfAccService.setAsClosed(tableView.getSelectedItems());
        } else if (reopenActionId.equals(actionId)) {
            chartOfAccService.reopenAccount(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        if (!confirmDialog("Delete?", "Are you really wish to delete selected accounts?")) {
            return;
        }
        chartOfAccService.deleteAccount(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<ChartOfAccounts> accountList = chartOfAccountsDao.loadAll();
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        ObservableList<ChartOfAccounts> observableList = FXCollections.observableArrayList(accountList);
        tableView.setItems(observableList);
    }

    private void changeCurrencyEvent() {
        CurrencyDialog dialog = new CurrencyDialog(getApplicationControl(), getPrimaryStage());
        dialog.showAndWait();
        if (dialog.isCancelled() || dialog.getSelected() == null) {
            return;
        }
        List<ChartOfAccounts> accountList = tableView.getSelectedItems();
        if (accountList != null) {
            chartOfAccService.updateCurrency(dialog.getSelected(), accountList);
            loadRecords();
        }
    }

    private void changeAccountTypeEvent() {
        EnumSelectionDialog<AccountType> dialog = new EnumSelectionDialog<>(getApplicationControl(), getPrimaryStage());
        dialog.setValueList(AccountType.values());
        dialog.showAndWait();
        if (dialog.isCancelled()) {
            return;
        }
        List<ChartOfAccounts> accountList = tableView.getSelectedItems();
        if (accountList != null) {
            chartOfAccService.updateAccountType(dialog.getSelected(), accountList);
            loadRecords();
        }
    }

    private void importFromExcelEvent() {
        setMessage("");
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<ChartOfAccounts> readExcelData = new ReadExcelData<>("", file, chartOfAccService);
        List<ChartOfAccounts> dataList = readExcelData.readRowData(chartOfAccService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            setMessage("Valid import records not found");
            return;
        }
        chartOfAccService.insertAccount(dataList);
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
        WriteExcelData<ChartOfAccounts> writeExcelData = new WriteExcelData<>(actionId, file, chartOfAccService);
        if (exportSelectedActionId.equals(actionId)) {
            List<ChartOfAccounts> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(chartOfAccountsDao.loadAll());
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
            case reopenActionId:
                statusChangedEvent(actionId);
                break;
            case changeCurrencyActionId:
                changeCurrencyEvent();
                break;
            case changeAccountTypeActionId:
                changeAccountTypeEvent();
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
