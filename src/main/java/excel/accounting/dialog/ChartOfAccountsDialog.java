package excel.accounting.dialog;

import excel.accounting.dao.ChartOfAccountsDao;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.ChartOfAccounts;
import excel.accounting.entity.Status;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Chart Of Accounts Dialog
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class ChartOfAccountsDialog extends AbstractDialog {
    private ReadableTableView<ChartOfAccounts> tableView;
    private SearchTextField searchTextField;
    private ChartOfAccountsDao chartOfAccountsDao;
    private AccountType[] accTypes;

    public ChartOfAccountsDialog(ApplicationControl control, Stage primaryStage, AccountType... accountTypes) {
        initialize(control, primaryStage);
        setAccountTypes(accountTypes);
    }

    public void setAccountTypes(AccountType... accountTypes) {
        this.accTypes = accountTypes;
    }

    @Override
    protected void onActionEvent(final String actionId) {
        if ("actionOkay".equals(actionId)) {
            hide();
        } else if ("actionCancel".equals(actionId)) {
            setCancelled(true);
            hide();
        }
    }

    @Override
    protected void onOpenEvent() {
        loadAccount();
    }

    private void loadAccount() {
        List<ChartOfAccounts> accounts = chartOfAccountsDao.searchAccount( //
                searchTextField.getText(), Status.Confirmed, accTypes);
        ObservableList<ChartOfAccounts> observableList = FXCollections.observableArrayList(accounts);
        tableView.setItems(observableList);
    }

    public ChartOfAccounts getSelected() {
        return tableView.getSelectedItem();
    }

    @Override
    protected Parent create() {
        chartOfAccountsDao = (ChartOfAccountsDao) getBean("chartOfAccountsDao");
        searchTextField = new SearchTextField();
        searchTextField.setActionHandler(actionId -> loadAccount());
        tableView = new ReadableTableView<ChartOfAccounts>().create();
        tableView.addTextColumn("code", "Account Number").setPrefWidth(140);
        tableView.addTextColumn("name", "Name").setPrefWidth(260);
        //
        VBox basePanel = new VBox();
        basePanel.setSpacing(24);
        basePanel.getChildren().addAll(searchTextField, tableView.getTableView());
        addAction("actionOkay", "Okay");
        addAction("actionCancel", "Cancel");
        return basePanel;
    }

    @Override
    protected String getTitle() {
        return "Chart Of Accounts Dialog";
    }
}
