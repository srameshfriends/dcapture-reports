package excel.accounting.view;

import excel.accounting.entity.Account;
import excel.accounting.model.RowData;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.AccountService;
import excel.accounting.shared.AbstractView;
import excel.accounting.shared.FileHelper;
import excel.accounting.shared.ViewHolder;
import excel.accounting.ui.DataListView;
import javafx.collections.FXCollections;
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
 * Account View
 */
public class AccountView extends AbstractView implements ViewHolder {
    private DataListView dataListView;
    private AccountService accountService;

    @Override
    public String getName() {
        return "accountView";
    }

    @Override
    public String getTitle() {
        return "Account";
    }

    @Override
    public Node createControl() {
        accountService = new AccountService();
        accountService.setDataProcessor(getDataProcessor());
        dataListView = new DataListView();
        dataListView.addIntegerColumn("id", "Id").setMinWidth(100);
        dataListView.addTextColumn("code", "Code").setMinWidth(220);
        dataListView.addTextColumn("name", "Name").setMinWidth(320);
        //
        VBox basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), dataListView.getTableView());
        return basePanel;
    }

    private HBox createToolbar() {
        Button importBtn = new Button("Import");
        importBtn.setOnAction(event -> importFromExcel());
        //
        Button exportBtn = new Button("Export");
        exportBtn.setOnAction(event -> exportToExcel());
        //
        HBox box = new HBox();
        box.setSpacing(12);
        box.setPadding(new Insets(12));
        box.getChildren().addAll(importBtn, exportBtn);
        return box;
    }

    @Override
    public void onResize(double width, double height) {
        dataListView.getTableView().setPrefWidth(width);
        loadRecords();
    }

    @Override
    public boolean canCloseView() {
        return true;
    }

    private void loadRecords() {
        List<Account> accountList = accountService.loadAll();
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        ObservableList<RowData> observableList = FXCollections.observableArrayList(accountList);
        dataListView.getTableView().setItems(observableList);
    }

    private void importFromExcel() {
        File file = FileHelper.showOpenFileDialogExcelOnly("Account Dialog", getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Account> readExcelData = new ReadExcelData<>(file, accountService);
        List<Account> rowDataList = readExcelData.readRowData(3, true);
        if (!rowDataList.isEmpty()) {
            accountService.insert(rowDataList);
        }
        loadRecords();
    }

    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getName());
        File file = fileChooser.showSaveDialog(getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData writeExcelData = new WriteExcelData(file, accountService);
        writeExcelData.writeRowData(getName(), accountService.loadAll());
    }
}
