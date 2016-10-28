package excel.accounting.dialog;

import excel.accounting.shared.ApplicationControl;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Abstract Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public abstract class AbstractDialog {
    private ApplicationControl applicationControl;
    private Stage dialogStage;

    public void initialize(Stage primaryStage, ApplicationControl applicationControl) {
        this.applicationControl = applicationControl;
        dialogStage = new Stage();
        dialogStage.setTitle(getTitle());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Parent parent = create();
        dialogStage.setScene(new Scene(parent));
    }

    protected ApplicationControl getApplicationControl() {
        return applicationControl;
    }

    public void show() {
        dialogStage.show();
    }

    protected abstract Parent create();

    protected abstract String getTitle();
}
