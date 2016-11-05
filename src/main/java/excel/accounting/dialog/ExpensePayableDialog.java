package excel.accounting.dialog;

import excel.accounting.dao.AccountDao;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Expense Payable Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExpensePayableDialog extends AbstractDialog {
    private AccountDao accountDao;
    private ReadableTableView<Account> expenseTable, incomeTable;
    private SearchTextField expenseSearchField, bankCashSearchField;
    private Label messageLabel;

    @Override
    protected String getTitle() {
        return "Expense Payment Dialog";
    }

    @Override
    protected void onActionEvent(final String actionId) {
        if ("actionOkay".equals(actionId)) {
            onCloseEvent();
        } else if ("actionCancel".equals(actionId)) {
            setCancelled(true);
            hide();
        }
    }

    private void loadExpenseAccounts() {
        String searchText = expenseSearchField.getText();
        List<Account> accountList = accountDao.findByAccountTypes(searchText, AccountType.Expense);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        expenseTable.setItems(observableList);
    }

    private void loadBankCashAccounts() {
        String searchText = bankCashSearchField.getText();
        List<Account> accountList = accountDao.findByAccountTypes(searchText, AccountType.Cash, AccountType.Bank);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        incomeTable.setItems(observableList);
    }

    @Override
    protected void onOpenEvent() {
        loadExpenseAccounts();
        loadBankCashAccounts();
    }

    @Override
    protected void onCloseEvent() {
        messageLabel.setText("");
        Account bankAccount = incomeTable.getSelectedItem();
        Account expenseAccount = expenseTable.getSelectedItem();
        if (bankAccount == null) {
            messageLabel.setText("Bank Account should not be empty");
            return;
        }
        if (expenseAccount == null) {
            messageLabel.setText("Expense Account should not be empty");
            return;
        }
        hide();
    }

    public Account getExpenseAccount() {
        return expenseTable.getSelectedItem();
    }

    public Account getIncomeAccount() {
        return incomeTable.getSelectedItem();
    }

    private ReadableTableView<Account> createExpenseTable() {
        ReadableTableView<Account> tableView = new ReadableTableView<Account>().create();
        tableView.setSelectionMode(SelectionMode.SINGLE);
        tableView.addTextColumn("accountNumber", "Account Number").setPrefWidth(100);
        tableView.addTextColumn("name", "Name").setPrefWidth(240);
        tableView.addTextColumn("currency", "Currency").setMinWidth(40);
        return tableView;
    }

    private ReadableTableView<Account> createBankTable() {
        ReadableTableView<Account> tableView = new ReadableTableView<Account>().create();
        tableView.setSelectionMode(SelectionMode.SINGLE);
        tableView.addTextColumn("accountNumber", "Account Number").setPrefWidth(100);
        tableView.addTextColumn("name", "Name").setPrefWidth(240);
        tableView.addTextColumn("currency", "Currency").setMinWidth(40);
        return tableView;
    }

    private Pane createExpenseControl() {
        expenseTable = createExpenseTable();
        expenseSearchField = new SearchTextField();
        expenseSearchField.setActionHandler(actionId -> loadExpenseAccounts());
        VBox vBox = new VBox();
        vBox.setSpacing(24);
        vBox.getChildren().addAll(expenseSearchField, expenseTable.getTableView());
        return vBox;
    }

    private Pane createBankCashControl() {
        incomeTable = createBankTable();
        bankCashSearchField = new SearchTextField();
        bankCashSearchField.setActionHandler(actionId -> loadBankCashAccounts());
        VBox vBox = new VBox();
        vBox.setSpacing(24);
        vBox.getChildren().addAll(bankCashSearchField, incomeTable.getTableView());
        return vBox;
    }

    @Override
    protected Parent create() {
        accountDao = (AccountDao) getBean("accountDao");
        messageLabel = new Label();
        HBox basePanel = new HBox();
        basePanel.getChildren().addAll(createExpenseControl(), createBankCashControl());
        addAction("actionOkay", "Okay");
        addAction("actionCancel", "Cancel");
        return basePanel;
    }
}
