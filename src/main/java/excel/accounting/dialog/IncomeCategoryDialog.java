package excel.accounting.dialog;

import excel.accounting.entity.IncomeCategory;
import excel.accounting.entity.Status;
import excel.accounting.service.IncomeCategoryService;
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
 * Income Category Dialog
 */
public class IncomeCategoryDialog extends AbstractDialog {
    private ReadableTableView<IncomeCategory> tableView;
    private SearchTextField searchTextField;
    private IncomeCategoryService incomeCategoryService;

    public IncomeCategoryDialog(ApplicationControl control, Stage primaryStage) {
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
        loadCurrency();
    }

    private void loadCurrency() {
        List<IncomeCategory> list = incomeCategoryService.searchIncomeCategory( //
                searchTextField.getText(), Status.Confirmed);
        ObservableList<IncomeCategory> observableList = FXCollections.observableArrayList(list);
        tableView.setItems(observableList);
    }

    public IncomeCategory getSelected() {
        return tableView.getSelectedItem();
    }

    @Override
    protected Parent create() {
        incomeCategoryService = (IncomeCategoryService) getBean("incomeCategoryService");
        searchTextField = new SearchTextField();
        searchTextField.setActionHandler(actionId -> loadCurrency());
        tableView = new ReadableTableView<IncomeCategory>().create();
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
        return "Income Category Dialog";
    }
}
