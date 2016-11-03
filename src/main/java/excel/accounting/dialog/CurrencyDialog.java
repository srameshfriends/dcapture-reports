package excel.accounting.dialog;

import excel.accounting.dao.CurrencyDao;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.service.CurrencyService;
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
 * Currency Dialog
 */
public class CurrencyDialog extends AbstractDialog {
    private ReadableTableView<Currency> tableView;
    private SearchTextField searchTextField;
    private CurrencyDao currencyDao;

    public CurrencyDialog(ApplicationControl control, Stage primaryStage) {
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
        List<Currency> currencyList = currencyDao.searchCurrency(searchTextField.getText(), Status.Confirmed);
        ObservableList<Currency> observableList = FXCollections.observableArrayList(currencyList);
        tableView.setItems(observableList);
    }

    public Currency getSelected() {
        return tableView.getSelectedItem();
    }

    @Override
    protected Parent create() {
        currencyDao = (CurrencyDao) getService("currencyDao");
        searchTextField = new SearchTextField();
        searchTextField.setActionHandler(actionId -> loadCurrency());
        tableView = new ReadableTableView<Currency>().create();
        tableView.addTextColumn("code", "Currency").setPrefWidth(120);
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
        return "Currency Dialog";
    }
}
