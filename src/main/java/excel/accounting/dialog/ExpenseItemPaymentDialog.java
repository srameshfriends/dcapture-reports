package excel.accounting.dialog;

import excel.accounting.dao.AccountDao;
import excel.accounting.dao.PaymentDao;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.ExpenseItem;
import excel.accounting.entity.Status;
import excel.accounting.service.PaymentService;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Expense Item Payment Dialog
 */
public class ExpenseItemPaymentDialog extends AbstractDialog {
    private ReadableTableView<ExpenseItem> expenseItemTableView;
    private ReadableTableView<Account> accountTableView;
    private SearchTextField accountSearchField;
    private AccountDao accountDao;
    private PaymentDao paymentDao;
    private PaymentService paymentService;
    private List<ExpenseItem> expenseItemList;

    public void setExpenseItemList(List<ExpenseItem> dataList) {
        this.expenseItemList = dataList;
    }

    private void calculateAmount() {

    }

    @Override
    protected void onOpenEvent() {
        if (expenseItemList == null || expenseItemList.isEmpty()) {
            return;
        }
        ObservableList<ExpenseItem> dataList = FXCollections.observableArrayList(expenseItemList);
        expenseItemTableView.setItems(dataList);
    }

    @Override
    protected String getTitle() {
        return "Expense Item Payments";
    }

    @Override
    protected Parent create() {
        paymentDao = (PaymentDao) getBean("paymentDao");
        paymentService = (PaymentService) getBean("paymentService");
        expenseItemTableView = new ReadableTableView<ExpenseItem>().create();
        expenseItemTableView.addTextColumn("code", "Item Code").setPrefWidth(90);
        expenseItemTableView.addTextColumn("expenseDate", "Expense Date").setPrefWidth(100);
        expenseItemTableView.addTextColumn("referenceNumber", "Reference Num").setPrefWidth(160);
        expenseItemTableView.addTextColumn("description", "Description").setPrefWidth(380);
        expenseItemTableView.addTextColumn("currency", "Currency").setMinWidth(80);
        expenseItemTableView.addTextColumn("amount", "Amount").setMinWidth(140);
        //
        VBox basePanel = new VBox();
        basePanel.setSpacing(24);
        basePanel.getChildren().addAll(expenseItemTableView.getTableView());
        addAction("actionOkay", "Okay");
        addAction("actionCancel", "Cancel");
        return basePanel;
    }

    private Node createBankAccounts() {
        accountDao = (AccountDao) getBean("accountDao");
        accountSearchField = new SearchTextField();
        accountSearchField.setActionHandler(actionId -> loadAccounts());
        accountTableView = new ReadableTableView<Account>().create();
        accountTableView.addTextColumn("code", "Account Number").setPrefWidth(100);
        accountTableView.addTextColumn("name", "Name").setPrefWidth(200);
        accountTableView.addTextColumn("currency", "Currency").setPrefWidth(80);
        //
        VBox panel = new VBox();
        panel.setSpacing(18);
        panel.getChildren().addAll(accountSearchField, accountTableView.getTableView());
        return panel;
    }

    private Node createAccounts() {
        return null;
    }

    private void loadAccounts() {
        List<Account> accounts = accountDao.searchAccount(accountSearchField.getText(), Status.Confirmed, //
                AccountType.Bank, AccountType.Cash);
        ObservableList<Account> observableList = FXCollections.observableArrayList(accounts);
        accountTableView.setItems(observableList);
    }

    @Override
    protected void onActionEvent(String actionId) {
        if ("actionOkay".equals(actionId)) {
            hide();
        } else if ("actionCancel".equals(actionId)) {
            setCancelled(true);
            hide();
        }
    }
}
