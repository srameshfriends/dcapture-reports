package excel.accounting.dialog;

import excel.accounting.dao.AccountDao;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.Status;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Account Dialog
 */
public class AccountDialog extends AbstractDialog {
    private ReadableTableView<Account> tableView;
    private SearchTextField searchTextField;
    private AccountDao accountDao;
    private AccountType[] accTypes;

    public AccountDialog(AccountType... accountTypes) {
        setAccountTypes(accountTypes);
    }

    private void setAccountTypes(AccountType... accountTypes) {
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
        List<Account> accounts = accountDao.searchAccount(searchTextField.getText(), Status.Confirmed, accTypes);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accounts);
        tableView.setItems(observableList);
    }

    public Account getSelected() {
        return tableView.getSelectedItem();
    }

    @Override
    protected Parent create() {
        accountDao = (AccountDao) getBean("accountDao");
        searchTextField = new SearchTextField();
        searchTextField.setActionHandler(actionId -> loadAccount());
        tableView = new ReadableTableView<Account>().create();
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
        return "Account Dialog";
    }
}
