package excel.accounting.client;

import excel.accounting.db.DataProcessor;
import excel.accounting.entity.Account;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.shared.ViewManager;
import excel.accounting.view.AccountView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main
 */
public class Main extends Application {

    private void addView(ViewManager viewManager) {
        viewManager.addView(new AccountView());
    }

    private void createTable(ApplicationControl control) {
        DataProcessor dataProcessor = control.getDataProcessor();
        dataProcessor.create(dataProcessor.createSchema());
        dataProcessor.create(new Account().createQuery());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ApplicationControl control = ApplicationControl.instance();
        ViewManager viewManager = new ViewManager();
        viewManager.start(primaryStage, control);
        addView(viewManager);
        viewManager.showView("accountView");
        createTable(control);
        primaryStage.setOnCloseRequest(event -> onApplicationCloseEvent());
    }

    private void onApplicationCloseEvent() {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
