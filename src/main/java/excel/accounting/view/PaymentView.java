package excel.accounting.view;

import excel.accounting.dao.PaymentDao;
import excel.accounting.entity.Payment;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.PaymentService;
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
 * Payment View
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class PaymentView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String draftedActionId = "draftedAction", confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction";

    private ReadableTableView<Payment> tableView;
    private PaymentDao paymentDao;
    private PaymentService paymentService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Expense, "paymentView", "Payments");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        paymentDao = (PaymentDao) getService("paymentDao");
        paymentService = (PaymentService) getService("paymentService");
        tableView = new ReadableTableView<Payment>().create();
        tableView.addTextColumn("accountNumber", "Payment Number").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(220);
        tableView.addTextColumn("description", "Description").setPrefWidth(260);
        tableView.addEnumColumn("accountType", "Payment Type").setMinWidth(120);
        tableView.addTextColumn("currency", "Currency").setMinWidth(60);
        tableView.addDecimalColumn("balance", "Payment Balance").setMinWidth(160);
        tableView.addTextColumn("status", "Status").setMinWidth(80);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Set As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Set As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Set As Closed");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Payments");
        tableView.addContextMenuItem(deleteActionId, "Delete Payments");
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
        if (!confirmDialog(message)) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            paymentService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            paymentService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            paymentService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        paymentService.deletePayment(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<Payment> accountList = paymentDao.loadAll();
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        ObservableList<Payment> observableList = FXCollections.observableArrayList(accountList);
        tableView.setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Payment> readExcelData = new ReadExcelData<>("", file, paymentService);
        List<Payment> dataList = readExcelData.readRowData(paymentService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            return;
        }
        paymentService.insertPayment(dataList);
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
        WriteExcelData<Payment> writeExcelData = new WriteExcelData<>(actionId, file, paymentService);
        if (exportSelectedActionId.equals(actionId)) {
            List<Payment> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(paymentDao.loadAll());
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
