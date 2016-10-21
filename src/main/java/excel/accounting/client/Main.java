package excel.accounting.client;

import excel.accounting.shared.ApplicationControl;
import excel.accounting.shared.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main
 */
public class Main extends Application {
    private ApplicationControl applicationControl;
    private ViewManager viewManager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        applicationControl = ApplicationControl.instance();
        viewManager = new ViewManager();
        viewManager.start(primaryStage, applicationControl);
        Registry.registerView(viewManager);
        Registry.registerService(applicationControl);
        viewManager.showView("accountView");

        primaryStage.setOnCloseRequest(event -> onApplicationCloseEvent());
    }

    private void onApplicationCloseEvent() {
        viewManager.closeAll();
        applicationControl.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
