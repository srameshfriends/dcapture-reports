package excel.accounting.dialog;

import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.service.AccountService;
import excel.accounting.ui.ReadableTableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Expense Payable Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExpensePayableDialog extends AbstractDialog {
    private AccountService accountService;
    private ReadableTableView<Account> expenseTable, bankTable;

    @Override
    protected String getTitle() {
        return "Expense Payment Dialog";
    }

    private void loadExpenseAccounts(String searchText) {
        List<Account> accountList = accountService.findAccountsByType(searchText, AccountType.IncomeExpense,
                AccountType.Expense);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        expenseTable.setItems(observableList);
    }

    private void loadBankCashAccounts(String searchText) {
        List<Account> accountList = accountService.findAccountsByType(searchText, AccountType.Cash,
                AccountType.Bank);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        bankTable.setItems(observableList);
    }

    private ReadableTableView<Account> createExpenseTable() {
        ReadableTableView<Account> tableView = new ReadableTableView<>();
        tableView.setSelectionMode(SelectionMode.SINGLE);
        tableView.addTextColumn("accountNumber", "Account Number").setPrefWidth(100);
        tableView.addTextColumn("name", "Name").setPrefWidth(240);
        tableView.addTextColumn("currency", "Currency").setMinWidth(40);
        return tableView;
    }

    private ReadableTableView<Account> createBankTable() {
        ReadableTableView<Account> tableView = new ReadableTableView<>();
        tableView.setSelectionMode(SelectionMode.SINGLE);
        tableView.addTextColumn("accountNumber", "Account Number").setPrefWidth(100);
        tableView.addTextColumn("name", "Name").setPrefWidth(240);
        tableView.addTextColumn("currency", "Currency").setMinWidth(40);
        return tableView;
    }

    @Override
    protected Parent create() {
        accountService = (AccountService) getApplicationControl().getService("accountService");
        HBox tablePanel = new HBox();
        expenseTable = createExpenseTable();
        bankTable = createBankTable();

        HBox actionPanel = new HBox();

        VBox basePanel = new VBox();
        basePanel.getChildren().addAll(tablePanel, actionPanel);
        return basePanel;
    }
}
