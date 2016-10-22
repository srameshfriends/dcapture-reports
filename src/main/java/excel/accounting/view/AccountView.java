package excel.accounting.view;

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
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Account View
 */
public class AccountView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String verifiedActionId = "verifiedAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

    private ReadableTableView<Account> readableTableView;
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
        readableTableView = new ReadableTableView<Account>().create();
        readableTableView.addIntegerColumn("id", "Id").setMaxWidth(80);
        readableTableView.addTextColumn("accountNumber", "Account Number").setPrefWidth(120);
        readableTableView.addTextColumn("name", "Name").setPrefWidth(220);
        readableTableView.addTextColumn("description", "Description").setPrefWidth(260);
        readableTableView.addTextColumn("category", "Category").setMinWidth(120);
        readableTableView.addTextColumn("currency", "Currency").setMinWidth(60);
        readableTableView.addDecimalColumn("balance", "Account Balance").setMinWidth(160);
        readableTableView.addTextColumn("status", "Status").setMinWidth(80);
        readableTableView.addSelectionChangeListener(viewListener);
        readableTableView.setContextMenuHandler(viewListener);
        readableTableView.addContextMenuItem(draftedActionId, "Change to Drafted");
        readableTableView.addContextMenuItem(verifiedActionId, "Change to Verified");
        readableTableView.addContextMenuItem(closedActionId, "Change to Closed");
        readableTableView.addContextMenuItem(exportSelectedActionId, "Export Accounts");
        readableTableView.addContextMenuItem(deleteActionId, "Delete Accounts");
        //
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), readableTableView.getTableView());
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
        if (!confirmDialog("Change Status", "Are you really wish to change selected accounts Status?")) {
            return;
        }
        if (verifiedActionId.equals(actionId)) {
            accountService.setAsVerified(readableTableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            accountService.setAsDrafted(readableTableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            accountService.setAsClosed(readableTableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        accountService.deleteAccount(readableTableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<Account> accountList = accountService.loadAll();
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        readableTableView.getTableView().setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcelOnly("Account Import Dialog", getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Account> readExcelData = new ReadExcelData<>("", file, accountService);
        List<Account> rowDataList = readExcelData.readRowData(accountService.getColumnNames().length, true);
        if (!rowDataList.isEmpty()) {
            accountService.insertAccount(rowDataList);
        }
        loadRecords();
    }

    /*
    id, account_number, name, category, status, currency, balance, description
    */
    private void exportToExcelEvent(final String actionId) {
        final String title = getViewConfig().getTitle();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        File file = fileChooser.showSaveDialog(getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<Account> writeExcelData = new WriteExcelData<>(actionId, file, accountService);
        if (exportSelectedActionId.equals(actionId)) {
            List<Account> selected = readableTableView.getSelectedItems();
            writeExcelData.writeRowData(title, selected);
        } else {
            writeExcelData.writeRowData(title, accountService.loadAll());
        }

    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        readableTableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, verifiedActionId,
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
            case verifiedActionId:
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
