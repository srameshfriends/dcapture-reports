package excel.accounting.dialog;

import excel.accounting.dao.ExpenseCategoryDao;
import excel.accounting.entity.ExpenseCategory;
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
 * Expense Category Dialog
 */
public class ExpenseCategoryDialog extends AbstractDialog {
    private ReadableTableView<ExpenseCategory> tableView;
    private SearchTextField searchTextField;
    private ExpenseCategoryDao expenseCategoryDao;

    public ExpenseCategoryDialog(ApplicationControl control, Stage primaryStage) {
        initialize(control, primaryStage);
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
        loadExpenseCategory();
    }

    private void loadExpenseCategory() {
        List<ExpenseCategory> list = expenseCategoryDao.searchExpenseCategory( //
                searchTextField.getText(), Status.Confirmed);
        ObservableList<ExpenseCategory> observableList = FXCollections.observableArrayList(list);
        tableView.setItems(observableList);
    }

    public ExpenseCategory getSelected() {
        return tableView.getSelectedItem();
    }

    @Override
    protected Parent create() {
        expenseCategoryDao = (ExpenseCategoryDao) getBean("expenseCategoryDao");
        searchTextField = new SearchTextField();
        searchTextField.setActionHandler(actionId -> loadExpenseCategory());
        tableView = new ReadableTableView<ExpenseCategory>().create();
        tableView.addTextColumn("code", "Category Code").setPrefWidth(120);
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
        return "Expense Category Dialog";
    }
}
