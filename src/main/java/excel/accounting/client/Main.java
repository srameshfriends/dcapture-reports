package excel.accounting.client;

import excel.accounting.shared.ApplicationControl;
import excel.accounting.ui.ViewManager;
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
        viewManager.setApplicationControl(applicationControl);
        viewManager.start(primaryStage);
        Registry.registerBean(applicationControl);
        Registry.registerView(viewManager);
        primaryStage.setOnCloseRequest(event -> onApplicationCloseEvent());
        viewManager.showView(null);
    }

    private void onApplicationCloseEvent() {
        viewManager.closeAll();
        applicationControl.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
