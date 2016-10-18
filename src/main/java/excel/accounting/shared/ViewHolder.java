package excel.accounting.shared;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * View Provider
 */
public interface ViewHolder {
    void setApplicationControl(ApplicationControl control);

    void setStage(Stage primaryStage);

    Stage getPrimaryStage();

    String getName();

    String getTitle();

    Node createControl();

    boolean canCloseView();

    void onResize(double width, double height);
}
