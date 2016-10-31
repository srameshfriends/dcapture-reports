package excel.accounting.dialog;

import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.service.CurrencyService;
import excel.accounting.ui.ReadableTableView;
import excel.accounting.ui.SearchTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Currency Link Dialog
 */
public class CurrencyLinkDialog extends AbstractDialog {
    private ReadableTableView<Currency> tableView;
    private SearchTextField searchTextField;
    private CurrencyService currencyService;

    @Override
    protected void onOpenEvent() {
        loadCurrency();
    }

    private void loadCurrency() {
        List<Currency> currencyList = currencyService.searchCurrency(searchTextField.getText(), Status.Confirmed);
        ObservableList<Currency> observableList = FXCollections.observableArrayList(currencyList);
        tableView.setItems(observableList);
    }

    @Override
    protected Parent create() {
        currencyService = (CurrencyService) getService("currencyService");
        searchTextField = new SearchTextField();
        searchTextField.setActionHandler(actionId -> loadCurrency());
        tableView = new ReadableTableView<Currency>().create();
        tableView.addTextColumn("code", "Currency").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(260);
        VBox vBox = new VBox();
        vBox.setSpacing(24);
        vBox.getChildren().addAll(searchTextField, tableView.getTableView());
        return vBox;
    }

    @Override
    protected String getTitle() {
        return "Currency Selection Dialog";
    }
}
