package excel.accounting.dialog;

import excel.accounting.db.*;
import excel.accounting.shared.ApplicationControl;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Abstract Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public abstract class AbstractDialog {
    private ApplicationControl applicationControl;
    private DataReader dataReader;
    private Stage dialogStage;
    private boolean cancelled;

    public void initialize(ApplicationControl control, Stage primaryStage) {
        this.applicationControl = control;
        dataReader = new DataReader(applicationControl.getDataProcessor());
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        Parent parent = create();
        parent.setStyle("-fx-border-color: #777");
        TitledPane titledPane = new TitledPane(getTitle(), parent);
        titledPane.setCollapsible(false);
        dialogStage.setScene(new Scene(titledPane));
    }

    protected DataReader getDataReader() {
        return dataReader;
    }

    protected Transaction createTransaction() {
        return new Transaction(applicationControl.getDataProcessor());
    }

    protected QueryBuilder getQueryBuilder(String sqlFileName, String queryName) {
        return applicationControl.getDataProcessor().getQueryBuilder(sqlFileName, queryName);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    protected ApplicationControl getApplicationControl() {
        return applicationControl;
    }

    protected Object getService(String name) {
        return applicationControl.getService(name);
    }

    public void show() {
        dialogStage.show();
        onOpenEvent();
    }

    public void showAndWait() {
        onOpenEvent();
        dialogStage.showAndWait();
    }

    public void hide() {
        dialogStage.hide();
    }

    protected Button createButton(String actionId, String title, EventHandler<ActionEvent> handler) {
        Button button = new Button(title);
        button.setId(actionId);
        button.setOnAction(handler);
        return button;
    }

    protected abstract Parent create();

    protected abstract String getTitle();

    protected void onOpenEvent() {
    }

    protected void onCloseEvent() {
    }
}
