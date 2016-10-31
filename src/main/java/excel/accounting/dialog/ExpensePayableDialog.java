package excel.accounting.dialog;

import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.service.AccountService;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
    private AccountService accountService;
    private ReadableTableView<Account> expenseTable, incomeTable;
    private SearchTextField expenseSearchField, bankCashSearchField;
    private Label messageLabel;

    @Override
    protected String getTitle() {
        return "Expense Payment Dialog";
    }

    private void loadExpenseAccounts() {
        String searchText = expenseSearchField.getText();
        List<Account> accountList = accountService.findAccountsByType(searchText, AccountType.IncomeExpense,
                AccountType.Expense);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        expenseTable.setItems(observableList);
    }

    private void loadBankCashAccounts() {
        String searchText = bankCashSearchField.getText();
        List<Account> accountList = accountService.findAccountsByType(searchText, AccountType.Cash,
                AccountType.Bank);
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
        accountService = (AccountService) getApplicationControl().getService("accountService");
        messageLabel = new Label();
        HBox tablePanel = new HBox();
        tablePanel.getChildren().addAll(createExpenseControl(), createBankCashControl());
        //
        Button okayBtn = createButton("actionOkay", "Okay", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onCloseEvent();
            }
        });
        Button cancelBtn = createButton("actionCancel", "Cancel", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setCancelled(true);
                hide();
            }
        });
        HBox actionPanel = new HBox();
        actionPanel.getChildren().add(messageLabel);
        actionPanel.setAlignment(Pos.CENTER_RIGHT);
        actionPanel.getChildren().addAll(okayBtn, cancelBtn);
        VBox basePanel = new VBox();
        basePanel.getChildren().addAll(tablePanel, actionPanel);
        return basePanel;
    }
}
