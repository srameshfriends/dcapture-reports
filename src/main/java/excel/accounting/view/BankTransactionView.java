package excel.accounting.view;

import excel.accounting.entity.BankTransaction;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.BankTransactionService;
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
 * Bank Transaction View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class BankTransactionView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

    private ReadableTableView<BankTransaction> tableView;
    private BankTransactionService bankTransactionService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Management, "bankTransactionView", "Bank Transactions");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        bankTransactionService = (BankTransactionService) getService("bankTransactionService");
        tableView = new ReadableTableView<BankTransaction>().create();
        tableView.addIntegerColumn("id", "Id").setPrefWidth(60);
        tableView.addTextColumn("bank", "Bank").setPrefWidth(60);
        tableView.addDateColumn("transactionDate", "Date").setPrefWidth(100);
        tableView.addIntegerColumn("transactionIndex", "Index").setPrefWidth(80);
        tableView.addTextColumn("transactionCode", "Transaction Code").setPrefWidth(180);
        tableView.addTextColumn("description", "Description").setPrefWidth(380);
        tableView.addTextColumn("currency", "Currency").setMinWidth(80);
        tableView.addDecimalColumn("creditAmount", "Credit Amount").setMinWidth(100);
        tableView.addDecimalColumn("debitAmount", "Debit Amount").setMinWidth(100);
        tableView.addTextColumn("status", "Status").setMinWidth(100);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Update As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Update As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Update As Closed");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Transaction");
        tableView.addContextMenuItem(deleteActionId, "Delete Transaction");
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
        if (!confirmDialog("Update Transaction Status",
                "Are you really wish to change selected transaction Status?")) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            bankTransactionService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            bankTransactionService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            bankTransactionService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        bankTransactionService.deleteBankTransaction(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<BankTransaction> bankTransactionList = bankTransactionService.loadAll();
        if (bankTransactionList == null || bankTransactionList.isEmpty()) {
            return;
        }
        ObservableList<BankTransaction> observableList = FXCollections.observableArrayList(bankTransactionList);
        tableView.setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<BankTransaction> readExcelData = new ReadExcelData<>("", file, bankTransactionService);
        List<BankTransaction> dataList = readExcelData.readRowData(bankTransactionService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            return;
        }
        List<Integer> existingIdList = bankTransactionService.findIdList();
        List<BankTransaction> updateList = new ArrayList<>();
        List<BankTransaction> insertList = new ArrayList<>();
        for (BankTransaction bankTransaction : dataList) {
            if (existingIdList.contains(bankTransaction.getId())) {
                updateList.add(bankTransaction);
            } else {
                insertList.add(bankTransaction);
            }
        }
        if (!updateList.isEmpty()) {
            bankTransactionService.updateBankTransaction(updateList);
        }
        if (!insertList.isEmpty()) {
            bankTransactionService.insertBankTransaction(insertList);
        }
        loadRecords();
    }

    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "transaction-" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<BankTransaction> writeExcelData = new WriteExcelData<>(actionId, file, bankTransactionService);
        if (exportSelectedActionId.equals(actionId)) {
            List<BankTransaction> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(bankTransactionService.loadAll());
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
