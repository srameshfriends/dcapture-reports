package excel.accounting.dialog;

import excel.accounting.dao.AccountDao;
import excel.accounting.dao.PaymentDao;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.ExpenseItem;
import excel.accounting.entity.Status;
import excel.accounting.forms.AbstractFormDialog;
import excel.accounting.forms.BigDecimalBinder;
import excel.accounting.forms.TextBinder;
import excel.accounting.service.PaymentService;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Expense Item Payment Dialog
 */
public class ExpenseItemPaymentDialog extends AbstractFormDialog {
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
        loadAccounts();
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
        HBox box = new HBox();
        box.getChildren().addAll(createAccountTable(), createAmountPane());
        VBox basePanel = new VBox();
        basePanel.getChildren().addAll(expenseItemTableView.getTableView(), box);
        //
        addAction("actionOkay", "Okay");
        addAction("actionCancel", "Cancel");
        return basePanel;
    }

    private VBox createAccountTable() {
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

    private Node createAmountPane() {
        TextBinder description = textBinder("description", "Description");
        BigDecimalBinder amount = bigDecimalField("amount", "Amount");
        GridPane gridPane = new GridPane();
        gridPane.add(description.getLabel(), 0, 0);
        gridPane.add(description.getField(), 1, 0);
        gridPane.add(amount.getLabel(), 0, 1);
        gridPane.add(amount.getField(), 1, 1);
        return gridPane;
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
