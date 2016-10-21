package excel.accounting.shared;

import excel.accounting.db.DataProcessor;
import javafx.stage.Stage;

/**
 * Abstract View
 */
public abstract class AbstractView implements ViewHolder {
    private Stage primaryStage;
    private ApplicationControl applicationControl;

    @Override
    public void setApplicationControl(ApplicationControl control) {
        this.applicationControl = control;
    }

    @Override
    public void setStage(final Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    protected DataProcessor getDataProcessor() {
        return applicationControl.getDataProcessor();
    }

    protected Object getService(String name) {
        return applicationControl.getService(name);
    }
}
