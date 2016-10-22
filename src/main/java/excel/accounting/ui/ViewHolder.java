package excel.accounting.ui;

import excel.accounting.shared.ApplicationControl;
import excel.accounting.ui.ViewConfig;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * View Provider
 */
public interface ViewHolder {
    void setApplicationControl(ApplicationControl control);

    void setStage(Stage primaryStage);

    Stage getPrimaryStage();

    ViewConfig getViewConfig();

    Node createControl();

    boolean canCloseView();

    void closeView();

    void openView(double width, double height);

    void onWidthChanged(double width);

    void onHeightChanged(double height);
}
