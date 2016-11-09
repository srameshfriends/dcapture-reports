package excel.accounting.dialog;

import excel.accounting.dao.AccountDao;
import excel.accounting.dao.ExpenseItemDao;
import excel.accounting.entity.*;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Expense Payable Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ExpensePayableDialog extends AbstractDialog {
    private AccountDao accountDao;
    private ExpenseItemDao expenseItemDao;
    private ReadableTableView<ExpenseItem> expenseTable;
    private ReadableTableView<Account> accountTable;

    private SearchTextField expenseSearchField, bankCashSearchField;
    private Label messageLabel;

    public ExpensePayableDialog(ApplicationControl control, Stage primaryStage) {
        initialize(control, primaryStage);
    }

    public void setExpenseItems(List<ExpenseItem> expenseItems) {
        if (expenseItems != null) {
            ObservableList<ExpenseItem> observableList = FXCollections.observableArrayList(expenseItems);
            expenseTable.setItems(observableList);
        }
    }

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
        List<ExpenseItem> expenseItemList = expenseItemDao.searchExpenseItems(searchText, Status.getConfirmed(),
                PaidStatus.getUnAndPartialPaid());
        ObservableList<ExpenseItem> observableList = FXCollections.observableArrayList(expenseItemList);
        expenseTable.setItems(observableList);
    }

    private void loadBankCashAccounts() {
        String searchText = bankCashSearchField.getText();
        List<Account> accountList = accountDao.findByAccountTypes(searchText, AccountType.Cash, AccountType.Bank);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accountList);
        accountTable.setItems(observableList);
    }

    @Override
    protected void onCloseEvent() {
        hide();
    }

    private Pane createExpenseControl() {
        expenseTable = new ReadableTableView<ExpenseItem>().create();
        expenseTable.setSelectionMode(SelectionMode.SINGLE);
        expenseTable.addTextColumn("code", "Code").setPrefWidth(100);
        expenseTable.addTextColumn("groupCode", "Group Code").setPrefWidth(100);
        expenseTable.addTextColumn("referenceNumber", "Reference").setPrefWidth(120);
        expenseTable.addTextColumn("description", "Description").setPrefWidth(220);
        expenseTable.addTextColumn("currency", "Currency").setPrefWidth(80);
        expenseTable.addDecimalColumn("amount", "amount").setMinWidth(60);
        expenseTable.addDecimalColumn("paidAmount", "Paid Amount").setMinWidth(60);

        expenseSearchField = new SearchTextField();
        expenseSearchField.setActionHandler(actionId -> loadExpenseAccounts());
        VBox vBox = new VBox();
        vBox.setSpacing(24);
        vBox.getChildren().addAll(expenseSearchField, expenseTable.getTableView());
        return vBox;
    }

    private Pane createAccountControl() {
        accountTable = new ReadableTableView<Account>().create();
        accountTable.setSelectionMode(SelectionMode.SINGLE);
        accountTable.addTextColumn("accountNumber", "Account Number").setPrefWidth(100);
        accountTable.addTextColumn("name", "Name").setPrefWidth(240);
        accountTable.addTextColumn("currency", "Currency").setMinWidth(40);
        bankCashSearchField = new SearchTextField();
        bankCashSearchField.setActionHandler(actionId -> loadBankCashAccounts());
        VBox vBox = new VBox();
        vBox.setSpacing(24);
        vBox.getChildren().addAll(bankCashSearchField, accountTable.getTableView());
        return vBox;
    }

    @Override
    protected Parent create() {
        accountDao = (AccountDao) getBean("accountDao");
        expenseItemDao = (ExpenseItemDao) getBean("expenseItemDao");
        messageLabel = new Label();
        SplitPane splitPane = new SplitPane(createExpenseControl(), createAccountControl());
        VBox basePanel = new VBox();
        basePanel.getChildren().addAll(messageLabel, splitPane);
        addAction("actionOkay", "Okay");
        addAction("actionCancel", "Cancel");
        return basePanel;
    }
}
