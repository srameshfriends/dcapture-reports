package excel.accounting.view;

import excel.accounting.dao.CurrencyDao;
import excel.accounting.db.*;
import excel.accounting.entity.Currency;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.CurrencyService;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.FileHelper;
import excel.accounting.ui.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Currency View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class CurrencyView extends AbstractView implements ViewHolder, EntityDao<Currency> {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction", reopenActionId = "reopenAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";
    private final int LOAD_DATA = 100, EXPORT_DATA = 110;

    private ReadableTableView<Currency> tableView;
    private CurrencyDao currencyDao;
    private CurrencyService currencyService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Registers, "currencyView", "Currency");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        currencyDao = (CurrencyDao) getService("currencyDao");
        currencyService = (CurrencyService) getService("currencyService");
        tableView = new ReadableTableView<Currency>().create();
        tableView.addTextColumn("code", "Currency").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(260);
        tableView.addTextColumn("symbol", "Symbol").setPrefWidth(100);
        tableView.addTextColumn("decimalPrecision", "Precision").setMinWidth(120);
        tableView.addTextColumn("status", "Status").setMinWidth(120);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(confirmedActionId, "Set As Confirmed");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(draftedActionId, "Set As Drafted");
        tableView.addContextMenuItem(closedActionId, "Set As Closed");
        tableView.addContextMenuItem(reopenActionId, "Reopen Currency");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(exportSelectedActionId, "Export As xls");
        tableView.addContextMenuItemSeparator();
        tableView.addContextMenuItem(deleteActionId, "Delete Currency");
        //
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), tableView.getTableView());
        return basePanel;
    }

    private HBox createToolbar() {
        final String importActionId = "importAction", refreshActionId = "refreshAction";
        Button refreshBtn, importBtn, exportBtn;
        refreshBtn = createButton(refreshActionId, "Refresh", event ->
                currencyDao.loadAll(LOAD_DATA, this)
        );
        importBtn = createButton(importActionId, "Import", event -> importFromExcelEvent());
        exportBtn = createButton(exportActionId, "Export", event -> exportToExcelEvent());
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
        currencyDao.loadAll(LOAD_DATA, this);
    }

    @Override
    public void onWidthChanged(double width) {
        basePanel.setPrefWidth(width);
    }

    @Override
    public void onHeightChanged(double height) {
        basePanel.setPrefHeight(height);
    }

    private void changeStatusEvent(String actionId) {
        String message = "";
        if (confirmedActionId.equals(actionId)) {
            message = "Are you really wish to confirmed?";
        } else if (draftedActionId.equals(actionId)) {
            message = "Are you really wish to drafted?";
        } else if (closedActionId.equals(actionId)) {
            message = "Are you really wish to closed?";
        } else if (reopenActionId.equals(actionId)) {
            message = "Are you really wish to reopen?";
        }
        if (!confirmDialog(message)) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            currencyService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            currencyService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            currencyService.setAsClosed(tableView.getSelectedItems());
        } else if (reopenActionId.equals(actionId)) {
            currencyService.reopenCurrency(tableView.getSelectedItems());
        }
        currencyDao.loadAll(LOAD_DATA, this);
    }

    private void deleteEvent() {
        if (!confirmDialog("Are you really wish to delete?")) {
            return;
        }
        currencyService.deleteCurrency(tableView.getSelectedItems());
        currencyDao.loadAll(LOAD_DATA, this);
    }

    @Override
    public void onEntityDaoCompleted(int pid, List<Currency> dataList) {
        switch (pid) {
            case LOAD_DATA:
                setItems(dataList);
                break;
            case EXPORT_DATA:
                exportToExcelEvent(dataList);
                break;
        }
    }

    private void setItems(List<Currency> dataList) {
        ObservableList<Currency> observableList = FXCollections.observableArrayList(dataList);
        tableView.setItems(observableList);
    }

    @Override
    public void onEntityDaoError(int pid, Exception ex) {
        ex.printStackTrace();
    }

    private void importFromExcelEvent() {
        setMessage("");
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Currency> readExcelData = new ReadExcelData<>("", file, currencyService);
        List<Currency> dataList = readExcelData.readRowData(currencyService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            setMessage("Valid import records not found");
            return;
        }
        currencyService.insertCurrency(dataList);
        currencyDao.loadAll(LOAD_DATA, this);
    }

    private void exportToExcelEvent() {
        currencyDao.loadAll(EXPORT_DATA, this);
    }

    private void exportToExcelEvent(List<Currency> currencyList) {
        String fileName = DataConverter.getUniqueFileName("currency", "xls");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Excel", "*.xls");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialFileName(fileName);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                File saveFile = fileChooser.showSaveDialog(getPrimaryStage());
                if (saveFile != null) {
                    File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
                    if (file != null) {
                        WriteExcelData<Currency> writeExcelData = new WriteExcelData<>("", file, currencyService);
                        writeExcelData.writeRowData(currencyList);
                    }
                }
            }
        });
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId,
                closedActionId);
    }

    private void performActionEvent(final String actionId) {
        setMessage("");
        switch (actionId) {
            case confirmedActionId:
            case draftedActionId:
            case closedActionId:
            case reopenActionId:
                changeStatusEvent(actionId);
                break;
            case deleteActionId:
                deleteEvent();
                break;
            case exportActionId:
                exportToExcelEvent();
                break;
            case exportSelectedActionId:
                exportToExcelEvent(tableView.getSelectedItems());
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
